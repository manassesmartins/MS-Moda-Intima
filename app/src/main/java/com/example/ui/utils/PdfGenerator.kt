package com.example.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.TransactionEntity
import com.example.data.OrderEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

fun generatePdfAndShare(
    context: Context,
    balance: Double,
    inflow: Double,
    outflow: Double,
    transactions: List<TransactionEntity>,
    orders: List<OrderEntity>,
    brandName: String
) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Colors
        val primaryColor = Color.parseColor("#381A2C")
        val secondaryColor = Color.parseColor("#8E6E82")
        val borderLight = Color.parseColor("#E0D6DD")

        // Draw title
        paint.color = primaryColor
        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText((brandName + " - RELATÓRIO OPERACIONAL").uppercase(Locale.getDefault()), 40f, 65f, paint)

        // Subtitle
        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("Demonstrativo Consolidado de Lucratividade, Produção e Custos", 40f, 85f, paint)

        // Metadata
        val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        paint.color = secondaryColor
        canvas.drawText("Emitido em: " + df.format(Date()), 390f, 85f, paint)

        // Line separator
        paint.strokeWidth = 1.5f
        paint.color = primaryColor
        canvas.drawLine(40f, 100f, 555f, 100f, paint)

        // Draw Section indicator titles
        paint.color = primaryColor
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Resumo Consolidado de Saúde Financeira", 40f, 130f, paint)

        paint.textSize = 11f
        paint.color = Color.BLACK
        paint.isFakeBoldText = false
        canvas.drawText(String.format("Faturamento Bruto (Receitas): R$ %,.2f", inflow), 50f, 160f, paint)
        canvas.drawText(String.format("Despesas Operacionais (Saídas): R$ %,.2f", outflow), 50f, 180f, paint)
        
        paint.color = primaryColor
        paint.isFakeBoldText = true
        paint.textSize = 11f
        canvas.drawText(String.format("Saldo em Caixa (Lucro Líquido): R$ %,.2f", balance), 50f, 210f, paint)

        // Draw helper KPI
        paint.color = Color.DKGRAY
        paint.isFakeBoldText = false
        val totalPieces = orders.sumOf { it.quantity }
        val costPiece = if (totalPieces > 0) outflow / totalPieces else 0.0
        val margin = if (inflow > 0.0) (balance / inflow) * 100.0 else 0.0
        canvas.drawText(String.format("Margem Estimada de Rendimento: %,.1f%%", margin), 50f, 230f, paint)
        canvas.drawText(String.format("Volume Total Fabricado: %d peças", totalPieces), 50f, 250f, paint)
        canvas.drawText(String.format("Custo de Insumo Unitário Médio: R$ %,.2f", costPiece), 50f, 270f, paint)

        // Draw Orders Table Title
        paint.color = primaryColor
        paint.isFakeBoldText = true
        paint.textSize = 12f
        canvas.drawText("Histórico Recente de Pedidos (Receitas)", 40f, 310f, paint)

        var currentY = 330f
        paint.textSize = 9f
        paint.isFakeBoldText = true
        paint.color = primaryColor
        canvas.drawText("Cliente", 45f, currentY, paint)
        canvas.drawText("Especificação", 180f, currentY, paint)
        canvas.drawText("Quant.", 340f, currentY, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Valor Total", 545f, currentY, paint)

        paint.strokeWidth = 1f
        paint.color = borderLight
        canvas.drawLine(40f, currentY+5, 555f, currentY+5, paint)
        currentY += 20f

        val limitedOrders = orders.take(8)
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        limitedOrders.forEach { o ->
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(o.clientName.uppercase(Locale.getDefault()).take(20), 45f, currentY, paint)
            canvas.drawText(o.pantyType + " (" + o.businessArea + ")", 180f, currentY, paint)
            canvas.drawText(o.quantity.toString() + " un", 340f, currentY, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format("R$ %,.2f", o.totalValue), 545f, currentY, paint)
            currentY += 15f
        }

        // Draw Costs Table Title
        currentY += 15f
        paint.textAlign = Paint.Align.LEFT
        paint.color = primaryColor
        paint.isFakeBoldText = true
        paint.textSize = 12f
        canvas.drawText("Histórico Recente de Gastos (Saídas)", 40f, currentY, paint)
        currentY += 20f

        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("Descrição", 45f, currentY, paint)
        canvas.drawText("Categoria", 220f, currentY, paint)
        canvas.drawText("Semana", 390f, currentY, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Preço", 545f, currentY, paint)

        paint.color = borderLight
        canvas.drawLine(40f, currentY+5, 555f, currentY+5, paint)
        currentY += 20f

        val limitTxs = transactions.filter { t -> t.type == "OUTFLOW" }.take(8)
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        limitTxs.forEach { t ->
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(t.description.take(28), 45f, currentY, paint)
            canvas.drawText(t.category, 220f, currentY, paint)
            canvas.drawText(t.week, 390f, currentY, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format("R$ %,.2f", t.amount), 545f, currentY, paint)
            currentY += 15f
        }

        pdfDocument.finishPage(page)

        // Write to cache directory to bypass FileProvider requirement securely
        val file = File(context.cacheDir, "Relatorio_Producao.pdf")
        if (file.exists()) {
            file.delete()
        }
        val stream = FileOutputStream(file)
        pdfDocument.writeTo(stream)
        pdfDocument.close()
        stream.close()

        Toast.makeText(context, "PDF pronto: " + file.name, Toast.LENGTH_SHORT).show()

        // Share via Intent using FileProvider to prevent crash
        val authority = "${context.packageName}.fileprovider"
        val fileUri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Exportar Relatório Produção")
            putExtra(Intent.EXTRA_SUBJECT, "Relatório Geral - $brandName")
            putExtra(Intent.EXTRA_TEXT, "Segue anexo o Relatório Executivo de Produção - $brandName.")
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório"))

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Erro ao exportar PDF: " + e.message, Toast.LENGTH_LONG).show()
    }
}

fun generateInvoicePdfAndShare(
    context: Context,
    order: OrderEntity,
    matchingOrders: List<OrderEntity>,
    brandName: String
) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Colors matching our elegant theme
        val darkPlumColor = Color.parseColor("#381A2C")
        val secondaryAccent = Color.parseColor("#8E6E82")
        val dividerColor = Color.parseColor("#D4C4CD")

        // Center / Header
        paint.color = darkPlumColor
        paint.textSize = 20f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(brandName.uppercase(Locale.getDefault()), 297.5f, 70f, paint)

        paint.textSize = 10f
        paint.isFakeBoldText = true
        paint.color = secondaryAccent
        canvas.drawText("DOCUMENTO DE FECHAMENTO SEMANAL", 297.5f, 92f, paint)

        paint.strokeWidth = 1.5f
        paint.color = darkPlumColor
        canvas.drawLine(40f, 108f, 555f, 108f, paint)

        // Metadata left-aligned
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 11f
        paint.isFakeBoldText = true
        paint.color = darkPlumColor
        canvas.drawText("CLIENTE:", 50f, 135f, paint)
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        canvas.drawText(order.clientName.uppercase(Locale.getDefault()), 120f, 135f, paint)

        paint.isFakeBoldText = true
        paint.color = darkPlumColor
        canvas.drawText("PERÍODO:", 50f, 155f, paint)
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        canvas.drawText(order.week, 120f, 155f, paint)

        paint.isFakeBoldText = true
        paint.color = darkPlumColor
        canvas.drawText("EMISSÃO:", 50f, 175f, paint)
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
        canvas.drawText(dateFormatter.format(Date(order.timestamp)), 120f, 175f, paint)

        paint.color = dividerColor
        canvas.drawLine(40f, 195f, 555f, 195f, paint)

        // Itemized Table Heading
        paint.textSize = 11f
        paint.isFakeBoldText = true
        paint.color = darkPlumColor
        canvas.drawText("ESPECIFICAÇÕES DOS PRODUTOS", 50f, 220f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL", 545f, 220f, paint)

        canvas.drawLine(40f, 230f, 555f, 230f, paint)

        // Items logic list
        var currentY = 255f
        paint.textSize = 10f
        matchingOrders.forEach { item ->
            paint.textAlign = Paint.Align.LEFT
            paint.isFakeBoldText = true
            paint.color = darkPlumColor
            canvas.drawText("${item.pantyType} (Tam ${item.pantySize})", 50f, currentY, paint)
            
            paint.textAlign = Paint.Align.RIGHT
            val formattedTotal = String.format(Locale("pt", "BR"), "R$ %,.2f", item.totalValue)
            canvas.drawText(formattedTotal, 545f, currentY, paint)
            
            currentY += 15f
            paint.textAlign = Paint.Align.LEFT
            paint.isFakeBoldText = false
            paint.color = Color.DKGRAY
            canvas.drawText("=> Quantidade: ${item.quantity} un  x  R$ ${String.format(Locale("pt", "BR"), "%,.2f", item.pantyValue)}", 60f, currentY, paint)
            
            paint.color = Color.BLACK
            currentY += 25f
        }

        paint.color = dividerColor
        canvas.drawLine(40f, currentY, 555f, currentY, paint)
        currentY += 25f

        // Grand Total row
        paint.textSize = 12f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT
        paint.color = darkPlumColor
        canvas.drawText("VALOR TOTAL DO FECHAMENTO:", 50f, currentY, paint)
        
        paint.textAlign = Paint.Align.RIGHT
        val grandTotalStr = String.format(Locale("pt", "BR"), "R$ %,.2f", matchingOrders.sumOf { it.totalValue })
        paint.color = Color.parseColor("#CC125C") // Highlight vibrant pinkaccent for grand total
        canvas.drawText(grandTotalStr, 545f, currentY, paint)

        pdfDocument.finishPage(page)

        val comandaFile = File(context.cacheDir, "Comanda_${order.clientName.replace(" ", "_")}.pdf")
        if (comandaFile.exists()) {
            comandaFile.delete()
        }
        val stream = FileOutputStream(comandaFile)
        pdfDocument.writeTo(stream)
        pdfDocument.close()
        stream.close()

        // Native share sheet menu trigger using FileProvider to prevent secure exposed crash
        val authority = "${context.packageName}.fileprovider"
        val comandaUri = FileProvider.getUriForFile(context, authority, comandaFile)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Compartilhar Comanda de Entrega")
            putExtra(Intent.EXTRA_SUBJECT, "Comanda Semanal - $brandName")
            putExtra(Intent.EXTRA_TEXT, "Prezado cliente, segue fechamento e comanda da semana referente à sua produção de confecções da marca $brandName.")
            putExtra(Intent.EXTRA_STREAM, comandaUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Enviar Comanda Semanal"))

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Erro ao gerar PDF da comanda: " + e.message, Toast.LENGTH_LONG).show()
    }
}

