import logging
from functools import lru_cache
from typing import Annotated

import boto3
from botocore.config import Config
from botocore.exceptions import ClientError
from fastapi import Depends
from fastapi.concurrency import run_in_threadpool

from .config import settings

log = logging.getLogger(__name__)

UPLOAD_SECONDS = 5 * 60
DOWNLOAD_SECONDS = 10 * 60


class Storage:
    """El bucket de fotos y documentos (S3 o compatible, como MinIO).

    Los archivos nunca pasan por la API: la API firma una URL y el teléfono sube o
    descarga directo del bucket. Hay dos clientes porque la firma de una URL de
    descarga incluye el host: se firma con el que ve el teléfono.
    """

    def __init__(self) -> None:
        config = Config(signature_version="s3v4", s3={"addressing_style": "path"})
        common = {
            "aws_access_key_id": settings.s3_access_key,
            "aws_secret_access_key": settings.s3_secret_key,
            "region_name": settings.s3_region,
            "config": config,
        }
        self._internal = boto3.client("s3", endpoint_url=settings.s3_endpoint_url, **common)
        self._public = boto3.client(
            "s3", endpoint_url=settings.s3_public_endpoint_url or settings.s3_endpoint_url, **common
        )
        self.bucket = settings.s3_bucket

    def presign_upload(self, key: str, content_type: str, max_bytes: int) -> dict:
        """Permiso de 5 minutos para subir un archivo de ese tipo y hasta ese tamaño."""
        return self._public.generate_presigned_post(
            Bucket=self.bucket,
            Key=key,
            Fields={"Content-Type": content_type},
            Conditions=[{"Content-Type": content_type}, ["content-length-range", 1, max_bytes]],
            ExpiresIn=UPLOAD_SECONDS,
        )

    def presign_get(self, key: str) -> str:
        """URL de 10 minutos para ver o descargar un archivo."""
        return self._public.generate_presigned_url(
            "get_object", Params={"Bucket": self.bucket, "Key": key}, ExpiresIn=DOWNLOAD_SECONDS
        )

    async def size_of(self, key: str) -> int | None:
        """Tamaño del archivo en el bucket, o None si todavía no se subió."""
        try:
            head = await run_in_threadpool(self._internal.head_object, Bucket=self.bucket, Key=key)
        except ClientError:
            return None
        return head["ContentLength"]

    async def delete(self, key: str) -> None:
        """Borra el archivo del bucket (S3 no se queja si ya no estaba)."""
        await run_in_threadpool(self._internal.delete_object, Bucket=self.bucket, Key=key)

    def ensure_bucket(self) -> None:
        """Crea el bucket si no existe. Solo se usa en desarrollo, con MinIO."""
        try:
            self._internal.head_bucket(Bucket=self.bucket)
        except ClientError:
            self._internal.create_bucket(Bucket=self.bucket)
            log.info("bucket %s creado", self.bucket)


@lru_cache
def get_storage() -> Storage:
    return Storage()


StorageDep = Annotated[Storage, Depends(get_storage)]
