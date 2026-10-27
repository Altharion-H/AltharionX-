package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.GeminiVisionService
import com.example.data.SystemScanReport
import com.example.ui.theme.CrimsonPenaltyRed
import com.example.ui.theme.MonarchPurple
import com.example.ui.theme.RankGold
import com.example.ui.theme.SystemAmoledBg
import com.example.ui.theme.SystemCyan
import com.example.ui.theme.SystemSurface
import com.example.ui.theme.SystemSurfaceBorder
import com.example.ui.theme.SystemSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import android.util.Base64

class WebAppInterface(private val onCapture: (String) -> Unit) {
    @JavascriptInterface
    fun onImageCaptured(base64Data: String) {
        onCapture(base64Data)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RealTimeCameraScanner(
    onScanCompleted: (SystemScanReport) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isAnalyzing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Gallery upload launcher fallback
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isAnalyzing = true
                errorMessage = null
                try {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    val base64Clean = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)

                    val report = GeminiVisionService.scanPhysiqueImageBase64(base64Clean)
                    if (!report.isValidPhysique) {
                        errorMessage = report.failureReason ?: "[SCAN FAILED: NO CLEAR TORSO/PHYSIQUE DETECTED. PLEASE ALIGN BODY WITHIN THE CAMERA GRID AND RE-SCAN]"
                        SystemFeedbackHelper.playBeepTone()
                    } else {
                        SystemFeedbackHelper.playSuccessFanfare()
                        onScanCompleted(report)
                    }
                } catch (e: Exception) {
                    errorMessage = "[SCAN ERROR]: ${e.message}"
                } finally {
                    isAnalyzing = false
                }
            }
        }
    }

    val htmlContent = remember {
        """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
          <style>
            * { margin:0; padding:0; box-sizing:border-box; }
            body, html { width:100%; height:100%; background:#07090E; overflow:hidden; font-family:monospace; }
            #container { position:relative; width:100%; height:100%; display:flex; flex-direction:column; align-items:center; justify-content:center; }
            video { width:100%; height:100%; object-fit:cover; position:absolute; top:0; left:0; z-index:1; }
            
            /* CYBERPUNK BIOMETRIC GRID OVERLAY */
            .overlay { position:absolute; top:0; left:0; width:100%; height:100%; z-index:2; pointer-events:none; }
            .laser { position:absolute; left:0; width:100%; height:2px; background:#00F0FF; box-shadow:0 0 12px #00F0FF, 0 0 24px #7B2CBF; animation:sweep 2.4s infinite ease-in-out; }
            @keyframes sweep {
              0% { top: 15%; opacity: 0.9; }
              50% { top: 85%; opacity: 1; }
              100% { top: 15%; opacity: 0.9; }
            }
            .shoulder-line { position:absolute; top:30%; left:15%; width:70%; height:1px; border-top:1.5px dashed rgba(0, 240, 255, 0.7); }
            .shoulder-label { position:absolute; top:27%; left:16%; font-size:10px; color:#00F0FF; letter-spacing:1px; }
            .waist-marker-left { position:absolute; top:58%; left:28%; width:18px; height:18px; border-left:2px solid #FF0055; border-bottom:2px solid #FF0055; }
            .waist-marker-right { position:absolute; top:58%; right:28%; width:18px; height:18px; border-right:2px solid #FF0055; border-bottom:2px solid #FF0055; }
            .waist-label { position:absolute; top:62%; left:50%; transform:translateX(-50%); font-size:9px; color:#FF0055; letter-spacing:1px; }
            .center-line { position:absolute; top:15%; left:50%; width:1px; height:70%; border-left:1px dashed rgba(255,255,255,0.2); }
            .reticle { position:absolute; top:20px; left:20px; width:20px; height:20px; border-top:2px solid #00F0FF; border-left:2px solid #00F0FF; }
            .reticle-tr { position:absolute; top:20px; right:20px; width:20px; height:20px; border-top:2px solid #00F0FF; border-right:2px solid #00F0FF; }
            .reticle-bl { position:absolute; bottom:20px; left:20px; width:20px; height:20px; border-bottom:2px solid #00F0FF; border-left:2px solid #00F0FF; }
            .reticle-br { position:absolute; bottom:20px; right:20px; width:20px; height:20px; border-bottom:2px solid #00F0FF; border-right:2px solid #00F0FF; }
            .hud-status { position:absolute; top:12px; left:50%; transform:translateX(-50%); font-size:10px; color:#00F0FF; background:rgba(7,9,14,0.7); padding:4px 10px; border-radius:4px; border:1px solid rgba(0,240,255,0.4); text-transform:uppercase; font-weight:bold; }
          </style>
        </head>
        <body>
          <div id="container">
            <video id="viewfinder" autoplay playsinline muted></video>
            <div class="overlay">
              <div class="hud-status">[LIVE BIOMETRIC VIEWFINDER]</div>
              <div class="laser"></div>
              <div class="shoulder-line"></div>
              <div class="shoulder-label">SHOULDER ALIGNMENT</div>
              <div class="waist-marker-left"></div>
              <div class="waist-marker-right"></div>
              <div class="waist-label">WAIST TAPER</div>
              <div class="center-line"></div>
              <div class="reticle"></div>
              <div class="reticle-tr"></div>
              <div class="reticle-bl"></div>
              <div class="reticle-br"></div>
            </div>
            <canvas id="captureCanvas" style="display:none;"></canvas>
          </div>

          <script>
            let currentStream = null;
            let currentFacing = "environment";

            async function startCamera(facing) {
              if (currentStream) {
                currentStream.getTracks().forEach(t => t.stop());
              }
              currentFacing = facing;
              try {
                const stream = await navigator.mediaDevices.getUserMedia({
                  video: { facingMode: currentFacing, width: { ideal: 1080 }, height: { ideal: 1920 } },
                  audio: false
                });
                currentStream = stream;
                const video = document.getElementById('viewfinder');
                video.srcObject = stream;
              } catch (err) {
                console.error("Camera error:", err);
              }
            }

            function toggleCamera() {
              const next = (currentFacing === "environment") ? "user" : "environment";
              startCamera(next);
            }

            function captureFrame() {
              const video = document.getElementById('viewfinder');
              const canvas = document.getElementById('captureCanvas');
              canvas.width = video.videoWidth || 720;
              canvas.height = video.videoHeight || 1280;
              const ctx = canvas.getContext('2d');
              ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
              
              const fullDataUrl = canvas.toDataURL('image/jpeg', 0.8);
              const base64Data = fullDataUrl.replace(/^data:image\/[a-z]+;base64,/, "");
              if (window.AndroidBridge) {
                window.AndroidBridge.onImageCaptured(base64Data);
              }
            }

            window.addEventListener('DOMContentLoaded', () => {
              startCamera("environment");
            });
          </script>
        </body>
        </html>
        """.trimIndent()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SystemAmoledBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REAL-TIME WEBRTC CAMERA SCANNER",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = SystemCyan
                    )
                    Text(
                        text = "ALIGN TORSO TO BIOMETRIC RETICLE",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Switch camera front/back
                    IconButton(
                        onClick = {
                            SystemFeedbackHelper.triggerHaptic(context)
                            webViewRef?.evaluateJavascript("toggleCamera();", null)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SystemSurfaceElevated)
                            .border(1.dp, SystemSurfaceBorder, CircleShape)
                            .testTag("switch_camera_facing_button")
                    ) {
                        Icon(Icons.Default.Cameraswitch, contentDescription = "Switch Camera", tint = SystemCyan, modifier = Modifier.size(18.dp))
                    }

                    // Close
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SystemSurfaceElevated)
                            .border(1.dp, SystemSurfaceBorder, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Live WebRTC Video Viewfinder with HTML5 Canvas capture
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, SystemCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.allowFileAccess = true
                            settings.cacheMode = WebSettings.LOAD_NO_CACHE

                            addJavascriptInterface(
                                WebAppInterface { base64Data ->
                                    coroutineScope.launch {
                                        isAnalyzing = true
                                        errorMessage = null
                                        try {
                                            val report = GeminiVisionService.scanPhysiqueImageBase64(base64Data)
                                            if (!report.isValidPhysique) {
                                                errorMessage = report.failureReason ?: "[SCAN FAILED: NO CLEAR TORSO/PHYSIQUE DETECTED. PLEASE ALIGN BODY WITHIN THE CAMERA GRID AND RE-SCAN]"
                                                SystemFeedbackHelper.playBeepTone()
                                            } else {
                                                SystemFeedbackHelper.playSuccessFanfare()
                                                onScanCompleted(report)
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "[SCAN ERROR]: ${e.message}"
                                        } finally {
                                            isAnalyzing = false
                                        }
                                    }
                                },
                                "AndroidBridge"
                            )

                            webChromeClient = object : WebChromeClient() {
                                override fun onPermissionRequest(request: PermissionRequest?) {
                                    request?.grant(request.resources)
                                }
                            }

                            webViewClient = object : WebViewClient() {}
                            loadDataWithBaseURL("https://localhost", htmlContent, "text/html", "UTF-8", null)
                            webViewRef = this
                        }
                    }
                )

                if (isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xD907090E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = SystemCyan, modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "GEMINI-2.5-FLASH MULTIMODAL SCAN IN PROGRESS...",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = SystemCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Evaluating skeletal frame, shoulder ratio, and fat %...",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Error Warning Banner if Scan Failed
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CrimsonPenaltyRed.copy(alpha = 0.15f))
                        .border(1.dp, CrimsonPenaltyRed, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPenaltyRed,
                        lineHeight = 15.sp
                    )
                }
            }

            // Bottom Actions Bar
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Gallery Upload Button
                Button(
                    onClick = {
                        SystemFeedbackHelper.triggerHaptic(context)
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(containerColor = SystemSurfaceElevated, contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SystemSurfaceBorder),
                    shape = CutCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("gallery_upload_scan_button")
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("GALLERY", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                // Capture & Analyze Physique Live Button
                Button(
                    onClick = {
                        SystemFeedbackHelper.triggerHaptic(context)
                        webViewRef?.evaluateJavascript("captureFrame();", null)
                    },
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = Color.Black),
                    shape = CutCornerShape(6.dp),
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                        .testTag("capture_analyze_physique_button")
                ) {
                    Icon(Icons.Default.Whatshot, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CAPTURE & ANALYZE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
