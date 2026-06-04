package com.example.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
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

        // Draw title
        paint.color = Color.BLACK
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText((brandName + " - RELATÓRIO OPERACIONAL").uppercase(Locale.getDefault()), 40f, 65f, paint)

        // Subtitle
        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("Produção e Confecção - Demonstrativo de Lucros e Custos", 40f, 85f, paint)

        // Metadata
        val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        canvas.drawText("Emitido em: " + df.format(Date()), 390f, 85f, paint)

        // Line separator
        paint.strokeWidth = 2f
        paint.color = Color.BLACK
        canvas.drawLine(40f, 100f, 555f, 100f, paint)

        // Draw Section indicator titles
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Resumo Consolidado de Saúde Financeira", 40f, 130f, paint)

        paint.textSize = 11f
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        canvas.drawText(String.format("Faturamento Bruto (Receitas): R$ %,.2f", inflow), 50f, 165f, paint)
        canvas.drawText(String.format("Despesas Operacionais (Saídas): R$ %,.2f", outflow), 50f, 185f, paint)
        
        paint.isFakeBoldText = true
        paint.textSize = 11f
        canvas.drawText(String.format("Saldo Caixa Retido (Lucro Líquido): R$ %,.2f", balance), 50f, 215f, paint)

        // Draw helper KPI
        paint.isFakeBoldText = false
        val totalPieces = orders.sumOf { it.quantity }
        val costPiece = if (totalPieces > 0) outflow / totalPieces else 0.0
        val margin = if (inflow > 0.0) (balance / inflow) * 100.0 else 0.0
        canvas.drawText(String.format("Margem Estimada de Rendimento: %,.1f%%", margin), 50f, 235f, paint)
        canvas.drawText(String.format("Volume Total Fabricado: %d peças", totalPieces), 50f, 255f, paint)
        canvas.drawText(String.format("Custo de Insumo Unitário Médio: R$ %,.2f", costPiece), 50f, 275f, paint)

        // Draw Orders Table Title
        paint.isFakeBoldText = true
        paint.textSize = 12f
        canvas.drawText("Histórico Detalhado de Encomendas (Receitas)", 40f, 315f, paint)

        var currentY = 335f
        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        canvas.drawText("Cliente", 45f, currentY, paint)
        canvas.drawText("Especificação", 160f, currentY, paint)
        canvas.drawText("Quant.", 320f, currentY, paint)
        canvas.drawText("Valor Total", 450f, currentY, paint)

        canvas.drawLine(40f, currentY+5, 555f, currentY+5, paint)
        currentY += 20f

        val limitedOrders = orders.take(8)
        limitedOrders.forEach { o ->
            canvas.drawText(o.clientName.take(18), 45f, currentY, paint)
            canvas.drawText(o.pantyType + " (" + o.businessArea + ")", 160f, currentY, paint)
            canvas.drawText(o.quantity.toString() + " un", 320f, currentY, paint)
            canvas.drawText(String.format("R$ %,.2f", o.totalValue), 450f, currentY, paint)
            currentY += 15f
        }

        // Draw Costs Table Title
        currentY += 15f
        paint.isFakeBoldText = true
        paint.textSize = 12f
        canvas.drawText("Histórico Detalhado dos Custos (Saídas)", 40f, currentY, paint)
        currentY += 20f

        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText("Descrição", 45f, currentY, paint)
        canvas.drawText("Categoria", 220f, currentY, paint)
        canvas.drawText("Semana", 370f, currentY, paint)
        canvas.drawText("Importe", 450f, currentY, paint)

        canvas.drawLine(40f, currentY+5, 555f, currentY+5, paint)
        currentY += 20f

        val limitTxs = transactions.filter { t -> t.type == "OUTFLOW" }.take(8)
        limitTxs.forEach { t ->
            canvas.drawText(t.description.take(24), 45f, currentY, paint)
            canvas.drawText(t.category, 220f, currentY, paint)
            canvas.drawText(t.week, 370f, currentY, paint)
            canvas.drawText(String.format("R$ %,.2f", t.amount), 450f, currentY, paint)
            currentY += 15f
        }

        pdfDocument.finishPage(page)

        // Write to cache directory to bypass FileProvider requirement securely
        val file = File(context.cacheDir, "Relatorio_Producao.pdf")
        val stream = FileOutputStream(file)
        pdfDocument.writeTo(stream)
        pdfDocument.close()
        stream.close()

        Toast.makeText(context, "PDF pronto: " + file.name, Toast.LENGTH_SHORT).show()

        // Share via Intent
        val sessionManager = com.example.data.SessionManager(context)
        val bName = sessionManager.appName
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Exportar Relatório Produção")
            putExtra(Intent.EXTRA_SUBJECT, "Relatório Geral - $bName")
            putExtra(Intent.EXTRA_TEXT, "Segue anexo o Relatório Executivo de Produção - $bName.")
            
            val fileUri = Uri.fromFile(file)
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório Finanças"))

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Erro ao exportar PDF: " + e.message, Toast.LENGTH_LONG).show()
    }
}
