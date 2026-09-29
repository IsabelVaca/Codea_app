package mx.tec.codea.ui.components

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import mx.tec.codea.R
import java.util.Locale

// the camera and the dictation are apps of the phone, not screens of ours.
// these two functions hide the permission and the launcher, and give back a
// simple "start" function, so the screens only receive a () -> Unit.

// asks for the camera permission if needed, takes a photo and gives it back.
@Composable
fun rememberTakePhoto(onPhotoTaken: (Bitmap) -> Unit): () -> Unit {
    val context = LocalContext.current
    val permissionMessage = stringResource(R.string.camera_permission_needed)

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        // the bitmap is null when the teacher closes the camera without a photo.
        if (bitmap != null) onPhotoTaken(bitmap)
    }
    val askPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            takePicture.launch(null)
        } else {
            Toast.makeText(context, permissionMessage, Toast.LENGTH_SHORT).show()
        }
    }

    return {
        if (hasPermission(context, Manifest.permission.CAMERA)) {
            takePicture.launch(null)
        } else {
            askPermission.launch(Manifest.permission.CAMERA)
        }
    }
}

// asks for the microphone permission if needed, listens, and gives back the text.
@Composable
fun rememberDictation(onTextHeard: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val prompt = stringResource(R.string.dictation_prompt)
    val permissionMessage = stringResource(R.string.dictation_permission_needed)
    val unavailableMessage = stringResource(R.string.dictation_unavailable)

    val recognizer = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) onTextHeard(spokenText)
        }
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
        }
        // some phones (and many emulators) have no voice service.
        try {
            recognizer.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, unavailableMessage, Toast.LENGTH_SHORT).show()
        }
    }

    val askPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            startListening()
        } else {
            Toast.makeText(context, permissionMessage, Toast.LENGTH_SHORT).show()
        }
    }

    return {
        if (hasPermission(context, Manifest.permission.RECORD_AUDIO)) {
            startListening()
        } else {
            askPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

private fun hasPermission(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
