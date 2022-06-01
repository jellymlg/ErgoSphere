const nanoERG = 1000000000;
const HEADERS = "Syncing headers", BLOCKS = "Syncing blocks", DONE = "Synchronized";
const OPCODE = ["Install", "Installing...", "Start", "Starting...", "Stop", "Stopping..."];

function update() {
    fetch(window.location.origin + "/status")
    .then(response => response.json())
    .then(data => {

        console.log(data);

        //general
        document.getElementById("bal").innerText = document.getElementById("toggle").classList.contains("nano") ? format(data.node.balance) : data.node.balance / nanoERG;
        document.getElementById("username").innerText = data.user;
        document.getElementById("fiat").innerText = "~ $" + (data.price * (data.node.balance / nanoERG));

        //home
        const hour = new Date().getHours(), hello = hour < 12 ? "morning" : (hour >= 12 && hour <= 17 ? "afternoon" : "evening");
        document.getElementById("homeTitle").innerHTML = "<p>good " + hello + ", " + data.user + "</p>";
        const nodeStatus = "<span>" + (data.node.status == 0 ? "Active" : (data.node.status == 1 ? "Loading" : "Inactive")) + "</span>", statusColor = data.node.status == 0 ? "green" : (data.node.status == 1 ? "#ffc107" : "gray");
        document.getElementById("walletStatus").innerHTML = nodeStatus;
        document.getElementById("walletStatus").style.color = statusColor;
        document.getElementById("ergBal").innerText = format(data.node.balance);
        document.getElementById("txs").innerHTML = data.node.txs.length == 0 ? "<p>No transactions</p>" : txList(data.node.txs);
        document.getElementById("nodeStatus").innerHTML = nodeStatus;
        document.getElementById("nodeStatus").style.color = statusColor;
        const full = data.node.fullHeight, header = data.node.headersHeight, peer = data.node.maxPeerHeight;
        const sync = (full == undefined ? (peer == undefined ? 0 : header / peer) : full / header) * 100, status = full == undefined ? HEADERS : BLOCKS;
        document.getElementById("syncStatus").innerText = sync.toFixed(2) + "%";
        document.getElementById("syncStatusLabel1").innerText = (sync >= 100 && status == BLOCKS ? DONE : status);
        document.getElementById("blocks1").innerHTML = blockList(data.node.blocks);
        const cpu = data.system.cpu, ram = data.system.memUsed / data.system.memFull * 100, hdd = data.system.hddUsed / data.system.hddFull * 100;
        document.getElementById("cpuNum").innerText = cpu.toFixed(2) + "%";
        document.getElementById("ramNum").innerText = ram.toFixed(2) + "%";
        document.getElementById("hddNum").innerText = hdd.toFixed(2) + "%";
        document.getElementById("cpuProg").style.background = "linear-gradient(90deg, " + color(cpu) + " " + (cpu.toFixed(2)) + "%, #222 0%)";
        document.getElementById("ramProg").style.background = "linear-gradient(90deg, " + color(ram) + " " + (ram.toFixed(2)) + "%, #222 0%)";
        document.getElementById("hddProg").style.background = "linear-gradient(90deg, " + color(hdd) + " " + (hdd.toFixed(2)) + "%, #222 0%)";
        document.getElementById("cpuDetails").innerText = data.system.proc + " process"
        document.getElementById("ramDetails").innerText = (data.system.memUsed / 1024).toFixed(1) + "GB / " + (data.system.memFull / 1024).toFixed(1) + "GB";
        document.getElementById("hddDetails").innerText = data.system.hddUsed.toFixed(1) + "GB / " + data.system.hddFull.toFixed(1) + "GB";

        //node
        document.getElementById("nodeTitleVersion").innerText = "v" + (data.node.appVersion == undefined ? "0" : data.node.appVersion);
        document.getElementById("nodeState").innerHTML = nodeStatus;
        document.getElementById("nodeState").style.color = statusColor;
        document.getElementById("blocks2").innerHTML = blockList(data.node.blocks);
        document.getElementById("syncStatusLabel2").innerText = (sync >= 100 && status == BLOCKS ? DONE : status);
        document.getElementById("syncProgress").style.background = "linear-gradient(90deg, " + (full == undefined ? "#ffc107" : "green") + " " + (sync.toFixed(2)) + "%, #222 0%)";
        document.getElementById("syncStatus2").innerText = sync.toFixed(2) + "%";

        //apps
        document.getElementById("apps").innerHTML = appList(data.apps, [2, 3, 4, 5]);

        //store
        document.getElementById("store").innerHTML = appList(data.apps, [0, 1]);

        document.body.style = "";
    });
    setTimeout(update, 5000);
}

function color(x) {
    return x <= 60 ? "green" : (x <= 80 ? "#ffc107" : "red");
}

function txList(arr) {
    let out = "";
    for(let i = 0; i < arr.length && i < 4; i++) {
        out += "<div class=\"tx\">";
        out += "<p class=\"value\">" + (arr[i].value > 0 ? "+ " : "- ") + format(Math.abs(arr[i].value)) + "</p>";
        out += "<p><img src=\"img/" + (arr[i].value < 0 ? "out" : "in") + ".svg\">" + (arr[i].value < 0 ? "Withdrawal" : "Deposit") + "</p>";
        out += "<p class=\"valueLabel\">nanoERG</p>";
        out += "<p class=\"time\">" + new Date(arr[i].time).toLocaleString() + "</p>";
        out += "</div>";
    }
    return out;
}

function blockList(arr) {
    if(arr.length == 0) for(var i = 0; i < 5; i++) arr[i] = {full: false, time: 0, txs: 0, num: 0};
    let out = "";
    for(let i = 0; i < arr.length && i < 4; i++) {
        out += "<div class=\"block\">";
        out += "<img src=\"img/" + (arr[i].full ? "block" : "header") + ".svg\">";
        out += "<p class=\"blockTimeLabel\">" + (Math.floor((new Date().getTime() - arr[i].time) / 60000)) + " minutes ago</p>";
        out += "<p class=\"blockNumber\">" + (arr[i].full ? "Block" : "Header") + " " + format(arr[i].num) + "</p>";
        out += "<p class=\"txCount\">" + arr[i].txs + " transactions</p>";
        out += "</div>";
    }
    return out += "<hr>";
}

function appList(arr, filter) {
    let out = "";
    for(let i = 0; i < arr.length; i++) if(filter.includes(arr[i].status)) {
        const s = OPCODE[arr[i].status], c = s.endsWith("...") ? OPCODE[arr[i].status - 1] : s, n = arr[i].name.replaceAll(" ", "").toLowerCase();
        out += "<div class=\"element card0\">";
        out += "<img src=\"img/brand/" + n + ".svg\">";
        out += "<p>" + arr[i].name + "</p>";
        out += "<p class=\"description\">" + arr[i].desc + "</p>";
        out += "<button class=\"" + c.toLowerCase() + (s.endsWith("...") ? " execDisabled" : "")
            +  "\" onclick=\'exec(this,\"" + s.toLowerCase() + "\",\"" + arr[i].name + "\", " + arr[i].status + ")\' "
            +  (s.endsWith("...") ? "disabled" : "") + ">" + s + "</button>";
        out += "</div>";
    }
    return out == "" ? "<p>No apps found</p>" : out;
}

function exec(e, m, s, o) {
    e.disabled = true;
    e.classList.add("execDisabled");
    e.innerText = OPCODE[o + 1];
    fetch(window.location.origin + "/do", {
        method: "POST",
        headers: {"Content-Type": "application/json"}, 
        body: JSON.stringify({
            exec: m,
            service: s
        })
    });
}

function checkUsername(u) {
    if(u.length == 0) return "The username cannot be empty.";
    if(u.length < 5) return "The username must be 5 or more characters long.";
    return "";
}

function checkPassword(p) {
    if(p.length == 0) return "The password cannot be empty.";
    if(p.length < 5) return "The password must be 5 or more characters long.";
    if(p != document.getElementById("init_password2").value) return "The two entered passwords must match.";
    if(p.match(/\d/g) == null || p.match(/\d/g).length < 2) return "The password must contain 2 or more numbers.";
    return "";
}

function newGlider() {
    return new Glider(document.querySelector(".glider"),{
        slidesToScroll: 1,
        slidesToShow: 1,
        duration: .5,
        scrollLock: true,
        draggable: false,
        dots: "#dots",
        easing: function (x, t, b, c, d) {
            return c * t / d + b;
        }
    });
}

function initLoad() {
    const glider = newGlider();
    document.querySelector(".glider").addEventListener("glider-slide-visible", function() {
        document.getElementById("next").innerHTML = glider.slide == 3 ? "Finish" : "Next";
    });
    document.getElementById("next").addEventListener("click", function() {
        glider.scrollItem(glider.slide + 1);
        if(document.getElementById("next").innerHTML == "Finish") {
            let user = document.getElementById("init_username").value, u;
            let pass = document.getElementById("init_password1").value, p;
            let mem  = document.getElementById("init_memory").value;
            if((u = checkUsername(user)) == "") {
                if((p = checkPassword(pass)) == "") {
                    fetch(window.location.origin + "/init", {
                        method: "POST",
                        headers: {"Content-Type": "application/json"}, 
                        body: JSON.stringify({
                            username: user,
                            password: pass,
                            memory: mem
                        })
                    }).then(res => res.json())
                    .then(data => {
                        if(data.status == "OK") window.location.href = window.location.origin;
                    });
                }else {
                    alert(p);
                    glider.scrollItem(2);
                }
            }else {
                alert(u);
                glider.scrollItem(1);
            }
        }
    });
    let memory = document.getElementById("init_memory"), mem_label = document.getElementById("init_memory_label");
    fetch(window.location.origin + "/init?mem", {
        method: "GET",
        headers: {"Content-Type": "application/json"}
    }).then(res => res.json())
    .then(data => memory.setAttribute("max", data.memory - 100));
    memory.addEventListener("input", function() {
        mem_label.innerText = memory.value + " MiB";
    });
    document.body.style = "";
}

function indexLoad() {
    const glider = newGlider();
    for(let i = 1; i <= 5; i++) document.getElementById("p" + i).addEventListener("click", function() {
        document.getElementsByClassName("page_active")[0].classList = "";
        this.classList = "page_active";
        document.title = "ErgoSphere - " + this.innerText;
        glider.scrollItem(i - 1);
    });
    document.getElementById("logo").addEventListener("click", function() {
        window.open("https://ergosphere.cloud", "_blank");
    });
    document.getElementById("logout").addEventListener("click", function() {
        if(confirm("Are you sure you want to log out?")) window.location.href = "http://x:y@" + window.location.host;
    });
    document.getElementById("toggle").addEventListener("click", function() {
        let bal = document.getElementById("bal");
        if(this.classList.toggle("nano")) {
            bal.innerText = format(+bal.innerText * nanoERG);
        }else {
            bal.innerText = +bal.innerText.replaceAll(",", "") / nanoERG;
        }
    });
    document.getElementById("shutdown").addEventListener("click", function() {
        if(confirm("Are you sure you want to shut ErgoSphere down?")) fetch(window.location.origin + "/shutdown");
    });
    document.getElementById("getAPIkey").addEventListener("click", function() {
        fetch(window.location.origin + "/?key").then(response => response.json()).then(x => alert(x.key));
    });
    update();
}

function format(x) {
    return x == undefined ? 0 : x.toLocaleString("EN");
}