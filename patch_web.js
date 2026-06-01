const fs = require('fs');

let html = fs.readFileSync('web/index.html', 'utf8');

// 1. Add MQTT script to head
html = html.replace(
    '</head>',
    '    <script src="https://cdnjs.cloudflare.com/ajax/libs/paho-mqtt/1.0.1/mqttws31.min.js"></script>\n</head>'
);

// 2. Replace #auth-panel
const authPanelRegex = /<div id="auth-panel"[\\s\\S]*?<!-- GOOGLE ACCOUNT PICKER MODAL/m;
const newAuthPanel = `
    <!-- LOGIN PANEL -->
    <div id="auth-panel" class="fixed inset-0 z-50 flex items-center justify-center p-4 overflow-y-auto" style="background: linear-gradient(180deg, var(--dark-bg, #1A0A13) 0%, rgba(244, 114, 182, 0.08) 50%, var(--dark-bg, #1A0A13) 100%);">
        <!-- Glowing background lights -->
        <div class="absolute top-10 left-10 w-72 h-72 rounded-full pointer-events-none blur-[120px]" style="background: radial-gradient(circle, rgba(244, 114, 182, 0.12) 0%, transparent 70%);"></div>
        <div class="absolute bottom-10 right-10 w-72 h-72 rounded-full pointer-events-none blur-[120px]" style="background: radial-gradient(circle, rgba(251, 207, 232, 0.08) 0%, transparent 70%);"></div>

        <div class="w-full max-w-md flex flex-col items-center gap-6 my-auto relative z-10 animate-fadeIn">
            <!-- Header Logo (matching Android Column) -->
            <div class="flex flex-col items-center text-center gap-1">
                <div class="w-16 h-16 rounded-full border-1.5 flex items-center justify-center mb-1 bg-white/5" style="border-color: var(--primary, #F472B6); border-width: 1.5px;">
                    <i id="auth-brand-icon" class="fa-solid fa-laptop text-2xl" style="color: var(--primary, #F472B6);"></i>
                </div>
                <span id="auth-brand-title" class="text-xs font-bold uppercase tracking-widest mt-1" style="color: var(--primary, #F472B6); letter-spacing: 2px;">GESTOR DE PRODUÇÃO</span>
                <h1 class="text-3xl font-black text-white tracking-tight">Conexão P2P com Celular</h1>
                <p class="text-xs opacity-60 text-purple-200">Sincronização Direta e Segura</p>
            </div>

            <!-- Main Auth Card -->
            <div class="glass-card w-full p-6 space-y-5 text-center flex flex-col items-center rounded-3xl z-20 shadow-xl" style="background: var(--card-bg, rgba(35, 13, 24, 0.75)); border: 1px solid var(--border-bg, rgba(226, 183, 206, 0.12));">
                <div class="space-y-1.5 w-full">
                    <span class="text-[10px] font-bold uppercase tracking-widest" style="color: var(--primary, #F472B6); letter-spacing: 1px;">CÓDIGO DE PAREAMENTO</span>
                    <p class="text-[11px] leading-relaxed opacity-75 text-purple-100 px-4">
                        Abra o aplicativo <strong>Gestor de Produção</strong> no seu celular, vá em <strong>Configurações > Conectar ao Computador / Web</strong> e digite o código de 6 dígitos abaixo.
                    </p>
                </div>
                
                <div class="bg-black/30 border border-white/5 rounded-2xl py-6 px-10 relative">
                    <div id="new-auth-pin" class="text-[44px] font-mono tracking-[0.3em] ml-[0.15em] font-black glow-text text-white" style="text-shadow: 0 0 15px var(--primary-glow)">000000</div>
                </div>

                <!-- Status messages -->
                <div id="auth-error-box" class="hidden w-full p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-xs text-red-400 text-center"></div>
                <div id="auth-success-box" class="hidden w-full p-3 rounded-xl bg-green-500/10 border border-green-500/30 text-xs text-green-400 text-center"></div>

                <!-- Connect status -->
                <div class="pt-2 flex flex-col items-center">
                    <div id="connection-status-spinner" class="w-6 h-6 rounded-full border-2 border-t-transparent animate-spin mb-3" style="border-color: var(--primary); border-top-color: transparent"></div>
                    <span id="connection-status-text" class="text-xs" style="color: var(--primary)">Aguardando aplicativo Android conectar...</span>
                </div>
            </div>
            
            <button onclick="continueOffline()" class="text-[10px] uppercase font-bold text-white/40 hover:text-white transition-colors pb-4 mt-2">
                <i class="fa-solid fa-cloud-slash mr-1"></i>Pular Pareamento e Criar Demonstração Local
            </button>
        </div>
    </div>

    <!-- P2P MQTT SCRIPT INJECTION -->
    <script>
        // Start MQTT listener for Auth
        let mqttClient;
        let p2pPinCode = Math.floor(100000 + Math.random() * 900000).toString();
        
        function initP2PMQTT() {
            setTimeout(() => {
                if (document.getElementById('new-auth-pin')) {
                    document.getElementById('new-auth-pin').innerText = p2pPinCode;
                }
            }, 100);

            // Using HiveMQ public websockets
            const clientId = "gestor-web-" + p2pPinCode + "-" + Math.floor(Math.random() * 1000);
            mqttClient = new Paho.MQTT.Client("broker.hivemq.com", 8000, "/mqtt", clientId);

            mqttClient.onConnectionLost = function (responseObject) {
                if (responseObject.errorCode !== 0) {
                    console.log("MQTT Connection Lost: " + responseObject.errorMessage);
                    setTimeout(initP2PMQTT, 3000); // Reconnect
                }
            };

            mqttClient.onMessageArrived = function (message) {
                console.log("MQTT Message Arrived on: " + message.destinationName);
                if (message.destinationName === "gestor_producao/sync/" + p2pPinCode) {
                    try {
                        const payload = JSON.parse(message.payloadString);
                        
                        // Show success 
                        const errBox = document.getElementById('auth-error-box');
                        const sucBox = document.getElementById('auth-success-box');
                        const spin = document.getElementById('connection-status-spinner');
                        const txt = document.getElementById('connection-status-text');
                        
                        if(errBox) errBox.classList.add('hidden');
                        if(spin) spin.classList.add('hidden');
                        if(txt) txt.innerText = "Sincronizando dados...";
                        
                        // Parse Database JSON from mobile
                        if (payload.transactions) transactions = payload.transactions;
                        if (payload.categories) categories = payload.categories;
                        if (payload.orders) orders = payload.orders;
                        if (payload.calculations) pieceCalculations = payload.calculations;
                        if (payload.brandConfig) brandConfig = payload.brandConfig;
                        
                        // Set mock user so app unblocks
                        user = {
                            id: "mobile-sync-user",
                            email: payload.email || "conectado@mobile.com",
                            created_at: new Date().toISOString()
                        };
                        
                        localStorage.setItem("ms_authenticated", "true");
                        localStorage.setItem("ms_auth_user", JSON.stringify(user));
                        
                        // Add success visuals and load app
                        if(sucBox) {
                            sucBox.classList.remove('hidden');
                            sucBox.innerHTML = '<i class="fa-solid fa-circle-check mr-1.5"></i>Sincronizado com o Celular com Sucesso!';
                        }
                        
                        setTimeout(() => {
                            document.getElementById('auth-panel').classList.add('hidden');
                            updateUserInfoUI();
                            loadBrandConfig(); // Applies theme if brandConfig was sent
                            renderTransactionsTable();
                            updateDashboardMetrics();
                            renderOrdersGrid();
                            renderCalculationsCards();
                            
                            // Let mobile app know we received it
                            const ack = new Paho.MQTT.Message(JSON.stringify({ status: "ok" }));
                            ack.destinationName = "gestor_producao/sync/" + p2pPinCode + "/ack";
                            mqttClient.send(ack);
                        }, 1200);

                    } catch(e) {
                        console.error("MQTT parsing error", e);
                    }
                }
            };

            const options = {
                timeout: 3,
                onSuccess: function() {
                    console.log("MQTT Connected");
                    mqttClient.subscribe("gestor_producao/sync/" + p2pPinCode, {qos: 1});
                },
                onFailure: function(message) {
                    console.log("MQTT Connection failed: " + message.errorMessage);
                    setTimeout(initP2PMQTT, 4000);
                }
            };
            
            try {
                mqttClient.connect(options);
            } catch(e) {
                console.error("Failed to connect MQTT", e);
            }
        }
        
        // Load on document ready
        document.addEventListener("DOMContentLoaded", function() {
            // Only start if not already authenticated
            if(!localStorage.getItem("ms_authenticated")) {
                setTimeout(initP2PMQTT, 500); 
            }
        });
    </script>
    <!-- GOOGLE ACCOUNT PICKER MODAL`;

html = html.replace(authPanelRegex, newAuthPanel);

fs.writeFileSync('web/index.html', html, 'utf8');

console.log('Web HTML patched successfully!');
