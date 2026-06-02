import java.io.File

tasks.register("patchWebAgain") {
    doLast {
        val htmlFile = File("web/index.html")
        var text = htmlFile.readText()

        // 1. Add QR Code Div & Library
        if (!text.contains("qrcode.min.js")) {
            text = text.replace(
                "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/paho-mqtt/1.0.1/mqttws31.min.js\"></script>",
                "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/qrcodejs/1.0.0/qrcode.min.js\"></script>\n            <script src=\"https://cdnjs.cloudflare.com/ajax/libs/paho-mqtt/1.0.1/mqttws31.min.js\"></script>"
            )
        }

        // 2. Add QR Code UI Container right below the pin div
        val pinBlockRegex = Regex("<div class=\"bg-black/30 border border-white/5 rounded-2xl py-5 px-10 relative mt-4\">.*?</div>", setOf(RegexOption.DOT_MATCHES_ALL))
        val newPinBlock = """
                        <div class="bg-black/30 border border-white/5 rounded-2xl py-5 px-10 relative mt-4">
                            <div id="new-auth-pin" class="text-[40px] font-mono tracking-[0.3em] font-black glow-text text-white pl-3" style="text-shadow: 0 0 15px var(--primary-glow)">000000</div>
                        </div>
                        
                        <div class="mt-2 text-center flex flex-col items-center">
                            <p class="text-[10px] text-purple-200/50 mb-2 uppercase tracking-wide">Ou Escaneie o QR Code</p>
                            <div id="qrcode-container" class="p-2 bg-white rounded-xl shadow-lg"></div>
                        </div>
        """.trimIndent()
        
        text = text.replace(pinBlockRegex, newPinBlock)

        // 3. Add QR Generation logic
        val initMqttRegex = Regex("function initP2PMQTT\\(\\) \\{")
        val withQr = """
                // Instantly show pin code
                document.addEventListener('DOMContentLoaded', () => {
                    const elm = document.getElementById('new-auth-pin');
                    if (elm) elm.innerText = p2pPinCode;
                    
                    const qrContainer = document.getElementById('qrcode-container');
                    if (qrContainer && typeof QRCode !== 'undefined') {
                        new QRCode(qrContainer, {
                            text: p2pPinCode,
                            width: 120,
                            height: 120,
                            colorDark : "#1A0A13",
                            colorLight : "#FFFFFF",
                            correctLevel : QRCode.CorrectLevel.M
                        });
                    }
                });

                function initP2PMQTT() {
        """.trimIndent()
        
        text = text.replace(initMqttRegex, withQr)

        htmlFile.writeText(text)
        println("Web QR code injected!")
    }
}
