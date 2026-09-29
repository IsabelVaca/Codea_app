package mx.tec.codea.ui.screens.report.report

import android.graphics.Bitmap
import mx.tec.codea.ui.screens.report.method.ReportMethod

/**
 * Estado UI para la pantalla de 'Nuevo reporte'.
 */
data class ReportUiState(
    val selectedMethod: ReportMethod = ReportMethod.PHOTO,
    val capturedPhoto: Bitmap? = null,
    val photoTime: String = "11:40",
    val photoLocation: String = "Tomada en el patio chico",
    val selectedReportType: ReportType = ReportType.INCIDENCIA,
    val descriptionText: String = "",
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * El botón continuar estará desbloqueado solo si el texto tiene al menos 10 caracteres
     */
    val isContinueEnabled: Boolean
        get() = descriptionText.trim().length >= 10
}
