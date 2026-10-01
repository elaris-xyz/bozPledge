package com.pledge.app.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareProofHelper {

    fun shareProofCard(
        context: Context,
        habitName: String = "10,000 Steps Daily Marathon",
        stakedAmount: Long = 1000L,
        currentDay: Int = 1,
        totalDays: Int = 7,
        seedVaultSig: String = "#5Kz8...7dKG"
    ) {
        val shareText = """
⚡ bozPLEDGE • Proof-of-Action Verified
━━━━━━━━━━━━━━━━━━━━
🐐 Boz (Mountain Goat) Endurance Protocol
🎯 Habit: $habitName
🔒 Smart Escrow: $stakedAmount ${'$'}SKR (Non-Custodial PDA)
✅ Status: Day $currentDay of $totalDays Verified on Solana Devnet
🛡️ Attestation: Seed Vault Biometric Digest ($seedVaultSig)
━━━━━━━━━━━━━━━━━━━━
🔥 Zero-Fee Guarantee: 0% dev take. Forfeited tokens are burned permanently.

🌐 Official Web: https://github.com/elaris-xyz/bozPledge
📦 Release APK: https://github.com/elaris-xyz/bozPledge/releases/tag/v1.0.0
        """.trimIndent()

        try {
            val imageFile = generateProofImage(context, habitName, stakedAmount, currentDay, totalDays, seedVaultSig)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Proof via"))
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to text if image generation or permission fails
            val textIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(textIntent, "Share Proof via"))
        }
    }

    private fun generateProofImage(
        context: Context,
        habitName: String,
        stakedAmount: Long,
        currentDay: Int,
        totalDays: Int,
        seedVaultSig: String
    ): File {
        val width = 1200
        val height = 750
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#07090E")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Glowing border
        val borderPaint = Paint().apply {
            color = Color.parseColor("#00FFA3")
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        val cardRect = RectF(24f, 24f, (width - 24).toFloat(), (height - 24).toFloat())
        canvas.drawRoundRect(cardRect, 32f, 32f, borderPaint)

        // Inner Card fill
        val innerCardPaint = Paint().apply {
            color = Color.parseColor("#0D111A")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, innerCardPaint)

        // Top Header
        val brandPaint = Paint().apply {
            color = Color.parseColor("#00FFA3")
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("⚡ bozPLEDGE • PROOF-OF-ACTION", 60f, 90f, brandPaint)

        val badgePaint = Paint().apply {
            color = Color.parseColor("#9945FF")
            textSize = 24f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("SOLANA DEVNET • MWA 2.0", (width - 450).toFloat(), 90f, badgePaint)

        // Divider
        val divPaint = Paint().apply {
            color = Color.parseColor("#1F2937")
            strokeWidth = 2f
        }
        canvas.drawLine(60f, 120f, (width - 60).toFloat(), 120f, divPaint)

        // Habit Title
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(habitName, 60f, 200f, titlePaint)

        // Status Box
        val statusBoxPaint = Paint().apply {
            color = Color.parseColor("#0A2818")
            style = Paint.Style.FILL
        }
        val statusStroke = Paint().apply {
            color = Color.parseColor("#00FFA3")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val statusRect = RectF(60f, 240f, 650f, 340f)
        canvas.drawRoundRect(statusRect, 16f, 16f, statusBoxPaint)
        canvas.drawRoundRect(statusRect, 16f, 16f, statusStroke)

        val statusTextPaint = Paint().apply {
            color = Color.parseColor("#00FFA3")
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("✔ Day $currentDay of $totalDays Certified", 90f, 302f, statusTextPaint)

        // Escrow details
        val labelPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 26f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        val valPaint = Paint().apply {
            color = Color.WHITE
            textSize = 32f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("STAKED ESCROW:", 60f, 410f, labelPaint)
        canvas.drawText("$stakedAmount \$SKR (Locked in PDA)", 60f, 455f, valPaint)

        canvas.drawText("HARDWARE ATTESTATION:", 60f, 520f, labelPaint)
        canvas.drawText("Seed Vault Biometric $seedVaultSig", 60f, 565f, valPaint)

        // Philosophy & Slogan Footer
        canvas.drawLine(60f, 610f, (width - 60).toFloat(), 610f, divPaint)

        val footerPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 24f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        canvas.drawText("Zero-Profit Deflationary Slashing • From your failure, NO ONE profits.", 60f, 665f, footerPaint)

        // Save to cache dir
        val cacheFolder = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(cacheFolder, "proof_badge.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
}
