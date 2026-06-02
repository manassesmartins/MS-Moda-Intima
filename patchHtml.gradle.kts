import java.io.File

tasks.register("patchWeb") {
    doLast {
        val htmlFile = File("web/index.html")
        var text = htmlFile.readText()

        // 1. Replace the Auth Panel div
        val oldAuthPanelRegex = Regex("<!-- LOGIN PANEL -->(.*?)(?=<!-- GOOGLE ACCOUNT PICKER MODAL)", setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))
        val newAuthPanel = """
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
                        <p class="text-[11px] opacity-60 text-purple-200 mt-2 text-center max-w-xs">Todos os dados e o logotipo carregado serão espelhados do seu dispositivo Android.</p>
                    </div>

                    <!-- Main Auth Card -->
                    <div class="glass-card w-full p-6 space-y-5 text-center flex flex-col items-center rounded-3xl z-20 shadow-xl" style="background: var(--card-bg, rgba(35, 13, 24, 0.75)); border: 1px solid var(--border-bg, rgba(226, 183, 206, 0.12));">
                        <div class="space-y-1.5 w-full">
                            <span class="text-[10px] font-bold uppercase tracking-widest" style="color: var(--primary, #F472B6); letter-spacing: 1px;">CÓDIGO DE PAREAMENTO</span>
                            <p class="text-[11px] leading-relaxed opacity-75 text-purple-100 px-4 mt-2">
                                Abra o aplicativo <strong>Gestor de Produção</strong> no seu celular, vá em <strong>Menu (⚙) > Configurações > Espelhamento PC / Web</strong> e digite o código de 6 dígitos abaixo.
                            </p>
                        </div>
                        
                        <div class="bg-black/30 border border-white/5 rounded-2xl py-5 px-10 relative mt-4">
                            <div id="new-auth-pin" class="text-[40px] font-mono tracking-[0.3em] font-black glow-text text-white pl-3" style="text-shadow: 0 0 15px var(--primary-glow)">000000</div>
                        </div>

                        <!-- Status messages -->
                        <div id="auth-error-box" class="hidden w-full p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-xs text-red-400 text-center"></div>
                        <div id="auth-success-box" class="hidden w-full p-3 rounded-xl bg-green-500/10 border border-green-500/30 text-xs text-green-400 text-center"></div>

                        <!-- Connect status -->
                        <div class="pt-4 flex flex-col items-center" id="connection-status-box">
                            <div id="connection-status-spinner" class="w-6 h-6 rounded-full border-2 border-t-transparent animate-spin mb-3" style="border-color: var(--primary); border-top-color: transparent"></div>
                            <span id="connection-status-text" class="text-xs font-semibold" style="color: var(--primary)">Aguardando aplicativo Android conectar...</span>
                        </div>
                    </div>
                    
                    <button onclick="continueOffline()" class="text-[10px] uppercase font-bold text-white/40 hover:text-white transition-colors pb-4 mt-2">
                        <i class="fa-solid fa-cloud-slash mr-1"></i>Pular Pareamento
                    </button>
                </div>
            </div>

            <!-- P2P MQTT SCRIPT INJECTION -->
            <script src="https://cdnjs.cloudflare.com/ajax/libs/paho-mqtt/1.0.1/mqttws31.min.js"></script>
            <script>
                let p2pPinCode = Math.floor(100000 + Math.random() * 900000).toString();
                let mqttClient = null;

                // Color converter helper missing in script
                function hexToRgbA(hex, alpha){
                    var c;
                    if(/^#([A-Fa-f0-9]{3}){1,2}$/.test(hex)){
                        c= hex.substring(1).split('');
                        if(c.length== 3){
                            c= [c[0], c[0], c[1], c[1], c[2], c[2]];
                        }
                        c= '0x'+c.join('');
                        return 'rgba('+[(c>>16)&255, (c>>8)&255, c&255].join(',')+','+alpha+')';
                    }
                    return 'rgba(255,255,255, '+alpha+')';
                }

                function applyBrandConfig(config) {
                    brandConfig = config;
                    localStorage.setItem('ms_brand_config', JSON.stringify(brandConfig));

                    // Apply theme
                    const themeMap = {
                        'PINK': { p: '#F472B6', s: '#FBCFE8', t: '#FDA4AF', bg: '#1A0A13' },
                        'EMERALD': { p: '#10B981', s: '#6EE7B7', t: '#34D399', bg: '#064E3B' },
                        'PURPLE': { p: '#8B5CF6', s: '#C4B5FD', t: '#A78BFA', bg: '#2E1065' },
                        'BLUE': { p: '#3B82F6', s: '#93C5FD', t: '#60A5FA', bg: '#172554' }
                    };
                    const scheme = themeMap[brandConfig?.colorScheme] || themeMap['PINK'];

                    document.documentElement.style.setProperty('--primary', scheme.p);
                    document.documentElement.style.setProperty('--primary-glow', hexToRgbA(scheme.p, 0.15));
                    document.documentElement.style.setProperty('--secondary', scheme.s);
                    document.documentElement.style.setProperty('--secondary-glow', hexToRgbA(scheme.s, 0.12));
                    document.documentElement.style.setProperty('--tertiary', scheme.t);
                    document.documentElement.style.setProperty('--tertiary-glow', hexToRgbA(scheme.t, 0.45));
                    document.documentElement.style.setProperty('--dark-bg', scheme.bg);

                    // Set UI names
                    const names = document.querySelectorAll('.dynamic-brand-name');
                    names.forEach(n => { n.innerText = config.brandName; });

                    // Set UI logos
                    const avatarContainer = document.getElementById('user-avatar-container');
                    if (avatarContainer) {
                        if (config.logoImage) {
                            avatarContainer.innerHTML = `<img src="${"$"}{config.logoImage}" class="w-full h-full object-cover rounded-full" alt="Logo">`;
                        } else {
                            avatarContainer.innerHTML = '<i class="fa-solid fa-gem text-lg text-primary/80"></i>';
                        }
                    }
                }

                function initP2PMQTT() {
                    const elm = document.getElementById('new-auth-pin');
                    if (elm) {
                        elm.innerText = p2pPinCode;
                    }

                    if (!window.Paho || !window.Paho.MQTT || !window.Paho.MQTT.Client) {
                        setTimeout(initP2PMQTT, 500);
                        return;
                    }

                    const clientId = "gestor-web-" + p2pPinCode + "-" + Math.floor(Math.random() * 1000);
                    mqttClient = new Paho.MQTT.Client("broker.hivemq.com", 8000, "/mqtt", clientId);

                    mqttClient.onConnectionLost = function (responseObject) {
                        setTimeout(initP2PMQTT, 3000); // Reconnect
                    };

                    mqttClient.onMessageArrived = function (message) {
                        if (message.destinationName === "gestor_producao/sync/" + p2pPinCode) {
                            try {
                                const payload = JSON.parse(message.payloadString);
                                
                                const sucBox = document.getElementById('auth-success-box');
                                const statusBox = document.getElementById('connection-status-box');
                                
                                if(statusBox) statusBox.classList.add('hidden');
                                
                                if (payload.brandConfig) {
                                    applyBrandConfig(payload.brandConfig);
                                }
                                
                                user = {
                                    id: "mobile-sync-user",
                                    email: "Sync (P2P Android)",
                                    created_at: new Date().toISOString()
                                };
                                localStorage.setItem("ms_user", JSON.stringify(user));
                                
                                if(sucBox) {
                                    sucBox.classList.remove('hidden');
                                    sucBox.innerHTML = '<i class="fa-solid fa-circle-check mr-1.5"></i>Pareamento concluído com sucesso!';
                                }
                                
                                setTimeout(() => {
                                    document.getElementById('auth-panel').classList.add('hidden');
                                    updateSyncBadge(true);
                                    // Make sure setup wizard is hidden
                                    const sw = document.getElementById('setup-wizard');
                                    if(sw) sw.classList.add('hidden');
                                    renderAll();
                                }, 1800);

                            } catch(e) {
                                console.error("MQTT parsing error", e);
                            }
                        }
                    };

                    const options = {
                        timeout: 3,
                        onSuccess: function() {
                            mqttClient.subscribe("gestor_producao/sync/" + p2pPinCode, {qos: 1});
                        },
                        onFailure: function(message) {
                            setTimeout(initP2PMQTT, 4000);
                        }
                    };
                    
                    try {
                        mqttClient.connect(options);
                    } catch(e) {}
                }
            </script>
        """
        text = text.replace(oldAuthPanelRegex, Regex.escapeReplacement(newAuthPanel))
        println("Replaced auth panel.")

        // 2. Remove the first-time setup modal 
        val setupModalRegex = Regex("<!-- BRAND SETUP PANEL \\(ONBOARDING\\).*?<!-- SIDEBAR NAVIGATION \\(LEFT\\) -->", setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))
        text = text.replace(setupModalRegex, Regex.escapeReplacement("<!-- SIDEBAR NAVIGATION (LEFT) -->"))
        println("Replaced setup wizard.")

        // 3. Patch the DOMContentLoaded listener
        val domReadyRegex = Regex("document\\.addEventListener\\('DOMContentLoaded', \\(\\) => \\{.*?\\}\\);", setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))
        val newDomReady = """
        document.addEventListener('DOMContentLoaded', () => {
            loadBrandConfig(); 
            const savedUser = localStorage.getItem('ms_user');
            if (savedUser) {
                user = JSON.parse(savedUser);
                document.getElementById('auth-panel').classList.add('hidden');
                
                const bs = localStorage.getItem('ms_brand_config');
                if (bs) {
                    try { applyBrandConfig(JSON.parse(bs)); } catch(e){}
                }

                updateSyncBadge(true);
                renderAll();
            } else {
                document.getElementById('auth-panel').classList.remove('hidden');
                setTimeout(initP2PMQTT, 500);
            }
        });
        """
        text = text.replace(domReadyRegex, Regex.escapeReplacement(newDomReady))
        println("Replaced init sequence.")

        htmlFile.writeText(text)
        println("Web HTML patched successfully!")
    }
}
