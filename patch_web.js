const fs = require('fs');

let html = fs.readFileSync('web/index.html', 'utf8');

const authPanelRegex = /<!-- LOGIN PANEL -->[\s\S]*?<!-- GOOGLE ACCOUNT PICKER MODAL/m;

const newAuthPanel = `
    <!-- LOGIN PANEL (MQTT P2P) -->
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
                    <div id="new-auth-pin" class="text-[44px] font-mono tracking-[0.3em] font-black glow-text text-white" style="text-shadow: 0 0 15px var(--primary-glow)">000000</div>
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
        let p2pPinCode = Math.floor(100000 + Math.random() * 900000).toString();
        let mqttClient = null;

        function initP2PMQTT() {
            const elm = document.getElementById('new-auth-pin');
            if (elm) {
                elm.innerText = p2pPinCode;
            }

            if (!window.Paho || !window.Paho.MQTT || !window.Paho.MQTT.Client) {
                console.error("MQTT client not loaded yet");
                setTimeout(initP2PMQTT, 1000);
                return;
            }

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
                        
                        const sucBox = document.getElementById('auth-success-box');
                        const spin = document.getElementById('connection-status-spinner');
                        const txt = document.getElementById('connection-status-text');
                        
                        if(spin) spin.classList.add('hidden');
                        if(txt) txt.innerText = "Sincronizando dados...";
                        
                        // Parse JSON
                        if (payload.transactions) {
                            transactions = [];
                            for(let i=0; i<payload.transactions.length(); i++) {
                                transactions.push(payload.transactions.getJSONObject(i)); 
                            }
                        }
                        if (payload.transactions) {
                            // Quick parse assuming payload is direct JSON array mapping
                            transactions = JSON.parse(JSON.stringify(payload.transactions));
                        }
                        if (payload.categories) {
                            categories = JSON.parse(JSON.stringify(payload.categories));
                        }
                        if (payload.orders) {
                            orders = JSON.parse(JSON.stringify(payload.orders));
                        }
                        if (payload.calculations) {
                            pieceCalculations = JSON.parse(JSON.stringify(payload.calculations));
                        }
                        if (payload.brandConfig) {
                            brandConfig = payload.brandConfig;
                            localStorage.setItem('ms_brand_config', JSON.stringify(brandConfig));
                            applyThemeFromConfig();
                        }
                        
                        user = {
                            id: "mobile-sync-user",
                            email: payload.email || "Android Sync (P2P)",
                            created_at: new Date().toISOString()
                        };
                        localStorage.setItem("ms_user", JSON.stringify(user));
                        
                        if(sucBox) {
                            sucBox.classList.remove('hidden');
                            sucBox.innerHTML = '<i class="fa-solid fa-circle-check mr-1.5"></i>Sincronizado com o Celular com Sucesso!';
                        }
                        
                        setTimeout(() => {
                            document.getElementById('auth-panel').classList.add('hidden');
                            updateSyncBadge(true, "Android Sync");
                            renderAll();
                        }, 1500);

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
                    
                    const spin = document.getElementById('connection-status-spinner');
                    const txt = document.getElementById('connection-status-text');
                    if (spin) spin.classList.remove('hidden');
                    if (txt) txt.innerText = "Escutando aplicativo Android na nuvem...";
                },
                onFailure: function(message) {
                    console.log("MQTT Connection failed: " + message.errorMessage);
                    setTimeout(initP2PMQTT, 4000);
                }
            };
            
            try {
                mqttClient.connect(options);
                console.log("Connecting MQTT per paho");
            } catch(e) {
                console.error("Failed to connect MQTT", e);
            }
        }
    </script>
    <!-- GOOGLE ACCOUNT PICKER MODAL`;

const newHTML = html.replace(authPanelRegex, newAuthPanel);

// Ensure MQTT library is there
let finalHTML = newHTML;
if (!finalHTML.includes('mqttws31.min.js')) {
    finalHTML = finalHTML.replace(
        '</head>',
        '    <script src="https://cdnjs.cloudflare.com/ajax/libs/paho-mqtt/1.0.1/mqttws31.min.js"></script>\n</head>'
    );
}

// Remove the Initial Setup Config Modal that the user complained about
const setupModalRegex = /<!-- BRAND SETUP WIZARD \(Runs on first ever login\) -->[\s\S]*?<!-- MAIN APP VIEW -->/m;
finalHTML = finalHTML.replace(setupModalRegex, `<!-- MAIN APP VIEW -->`);

// Replace the DOMContentLoaded app init block to call initP2PMQTT if not logged in
const documentReadyRegex = /document\.addEventListener\('DOMContentLoaded', \(\) => {[\s\S]*?loadBrandConfig\(\);[\s\S]*?updateSyncBadge\(false\);[\s\S]*?}\);/m;
const newDOMReady = `document.addEventListener('DOMContentLoaded', () => {
        loadBrandConfig();
        const savedUser = localStorage.getItem('ms_user');
        if (savedUser) {
            user = JSON.parse(savedUser);
            document.getElementById('auth-panel').classList.add('hidden');
            updateSyncBadge(true, "Android Sync");
            renderAll();
        } else {
            document.getElementById('auth-panel').classList.remove('hidden');
            setTimeout(initP2PMQTT, 800);
        }
    });`;
finalHTML = finalHTML.replace(documentReadyRegex, newDOMReady);

fs.writeFileSync('web/index.html', finalHTML, 'utf8');
console.log('Patch complete.');
