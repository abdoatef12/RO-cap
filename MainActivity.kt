package com.rosto.editor

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Effects
import androidx.media3.effect.Presentation
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.media3.ui.PlayerView
import java.io.File

@UnstableApi
class MainActivity : Activity() {
    private var selectedUri: Uri? = null
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var status: TextView
    private lateinit var exportButton: Button
    private var exportedFile: File? = null

    private val pickVideo = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedUri = uri
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) { }
            player?.setMediaItem(MediaItem.fromUri(uri))
            player?.prepare()
            player?.playWhenReady = false
            status.text = "تم اختيار الفيديو. راجعه ثم صدّره عموديًا."
            exportButton.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(13, 15, 23)
        window.navigationBarColor = Color.rgb(13, 15, 23)
        buildUi()
        player = ExoPlayer.Builder(this).build()
        playerView.player = player
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 28, 22, 20)
            setBackgroundColor(Color.rgb(13, 15, 23))
        }
        fun label(text: String, size: Float, color: Int, bold: Boolean = false) =
            TextView(this).apply {
                this.text = text
                textSize = size
                setTextColor(color)
                if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 8, 0, 8)
            }

        root.addView(label("ROSTO EDITOR", 14f, Color.rgb(190, 170, 255), true))
        root.addView(label("محرر فيديو مجاني للأندرويد", 25f, Color.WHITE, true))
        root.addView(label("نسخة تجريبية 0.1 • معاينة وتصدير عمودي", 14f, Color.LTGRAY))

        playerView = PlayerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            ).apply { topMargin = 16; bottomMargin = 16 }
            setBackgroundColor(Color.BLACK)
            useController = true
        }
        root.addView(playerView)

        root.addView(Button(this).apply {
            text = "اختيار فيديو من الهاتف"
            setOnClickListener { pickVideo.launch(arrayOf("video/*")) }
        })

        exportButton = Button(this).apply {
            text = "تصدير عمودي 1080 × 1920"
            isEnabled = false
            setOnClickListener { exportVertical() }
        }
        root.addView(exportButton)

        root.addView(Button(this).apply {
            text = "مشاركة آخر فيديو مُصدّر"
            setOnClickListener {
                val file = exportedFile
                if (file == null || !file.exists()) {
                    status.text = "لا يوجد تصدير بعد."
                } else {
                    val uri = FileProvider.getUriForFile(this@MainActivity, "$packageName.files", file)
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "video/mp4"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(share, "مشاركة الفيديو"))
                }
            }
        })

        status = label("اختر فيديو للبدء.", 13f, Color.rgb(190, 200, 215)).apply {
            gravity = Gravity.CENTER
        }
        root.addView(status)
        setContentView(root)
    }

    private fun exportVertical() {
        val uri = selectedUri ?: return
        exportButton.isEnabled = false
        status.text = "جارٍ التصدير… قد يستغرق وقتًا حسب طول الفيديو."
        val dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: filesDir
        val out = File(dir, "ROSTO_${System.currentTimeMillis()}.mp4")
        val presentation = Presentation.createForWidthAndHeight(
            1080, 1920, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP
        )
        val edited = EditedMediaItem.Builder(MediaItem.fromUri(uri))
            .setEffects(Effects(emptyList(), listOf(presentation)))
            .build()
        val transformer = Transformer.Builder(this)
            .setVideoMimeType("video/avc")
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    exportedFile = out
                    status.text = if (out.exists() && out.length() > 0) {
                        "نجح التصدير والتحقق من الملف: ${out.name} • ${out.length() / 1024 / 1024} MB"
                    } else "انتهى التصدير لكن لم يتم العثور على ملف صالح."
                    exportButton.isEnabled = true
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    status.text = "فشل التصدير: ${exportException.message ?: "خطأ غير معروف"}"
                    exportButton.isEnabled = true
                }
            }).build()
        transformer.start(edited, out.absolutePath)
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }
}
