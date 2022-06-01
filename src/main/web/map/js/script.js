var root = am5.Root.new("chart");
root.setThemes([
    am5themes_Animated.new(root)
]);
var chart = root.container.children.push(
    am5map.MapChart.new(root, {
        panX: "rotateX",
        panY: "translateY",
        projection: am5map.geoMercator()
    })
);
var cont = chart.children.push(
  am5.Container.new(root, {
    layout: root.horizontalLayout,
    x: 0,
    y: 0
  })
);
var backgroundSeries = chart.series.push(am5map.MapPolygonSeries.new(root, {}));
backgroundSeries.mapPolygons.template.setAll({
    fill: "transparent",
    fillOpacity: 0,
    strokeOpacity: 0
});
backgroundSeries.data.push({
    geometry: am5map.getGeoRectangle(-700, -700, 700, 700)
});
var polygonSeries = chart.series.push(
    am5map.MapPolygonSeries.new(root, {
        geoJSON: am5geodata_worldLow,
        exclude: ["AQ"]
    })
);
polygonSeries.mapPolygons.template.setAll({
    fill: "#4500ff",
    fillOpacity: 1,
    strokeWidth: 1,
    stroke: "white"
});
var lineSeries = chart.series.push(am5map.MapLineSeries.new(root, {}));
lineSeries.mapLines.template.setAll({
    stroke: "black",
    strokeOpacity: 1
});
var pointSeries = chart.series.push(am5map.MapPointSeries.new(root, {}));
pointSeries.bullets.push(function (x, y, obj) {
    NODES[obj.dataContext.i].visual = am5.Container.new(root, {});
    let pic = am5.Picture.new(root, {
        width: 16,
        height: 16,
        dx: -8,
        dy: -8,
        i: obj.dataContext.i,
        src: "img/erg_" + obj.dataContext.stat + ".png",
        fill: obj.dataContext.stat == "on" ? "#ff5031" : "gray",
        tooltipY: am5.percent(10),
        tooltipText: "[bold]{name}\n{ip}",
    });
    NODES[obj.dataContext.i].visual.children.push(pic);
    pic.events.on("click", function(ev) {
        inspectElem(ev.target._settings.i);
    });
    pic.events.on("pointerover", function(ev) {
        document.body.style.cursor = "pointer";
        //lookup
    });
    pic.events.on("pointerout", function(ev) {
        document.body.style.cursor = "default";
    });
    return am5.Bullet.new(root, {
        sprite: NODES[obj.dataContext.i].visual
    });
});

document.getElementById("close").addEventListener("click", function() {
    NODES[this.getAttribute("i")].visual.children.removeIndex(1);
    this.removeAttribute("i");
    document.getElementById("inspect").style.display = "";
    document.getElementById("data").innerHTML = "";
    document.getElementById("default").style.display = "";
});
document.getElementById("search").addEventListener("input", function() {
    for(let i = 0; i < NODES.length; i++) filter(i, this.value);
});
document.getElementById("clear").addEventListener("click", function() {
    if(confirm("Do you really want to clear the cache?")) {
        CACHE = [];
        window.location.href = window.location.href;
    }
});

function filter(i, val) {
    document.querySelectorAll("div[i=\"" + i + "\"]")[0].style.display = NODES[i].address.indexOf(val) > -1 || NODES[i].name.indexOf(val) > -1 ? "block" : "none";
}

function addNode(i) {
    if(addedIPs.includes(NODES[i].address)) return;
    pointSeries.data.push({
        geometry: { type: "Point", coordinates: [NODES[i].longitude, NODES[i].latitude] },
        name: NODES[i].name,
        ip: NODES[i].address,
        stat: NODES[i].status,
        i: i
    });
    let elem = document.createElement("div"), name = document.createElement("p"), ip = document.createElement("p"), country = document.createElement("img");
    name.innerText = NODES[i].name;
    ip.innerText = NODES[i].address;
    country.src = "img/country/" + NODES[i].countryCode.toLowerCase() + ".png";
    country.title = NODES[i].country;
    elem.appendChild(name);
    elem.appendChild(country);
    elem.appendChild(ip);
    elem.setAttribute("i", i);
    elem.addEventListener("click", function() {inspectElem(this.getAttribute("i"));});
    document.getElementById("list").appendChild(elem);
    filter(i, document.getElementById("search").value);
}

function inspectElem(i) {
    if(document.getElementById("close").getAttribute("i") != null) {
        NODES[document.getElementById("close").getAttribute("i")].visual.children.removeIndex(1);
        document.getElementById("data").innerHTML = "";
    }
    let circle = NODES[i].visual.children.push(am5.Circle.new(root, {
        radius: 4,
        fill: NODES[i].status == "on" ? "#ff5031" : "gray",
        strokeOpacity: 0,
    }));
    circle.animate({
        key: "scale",
        from: 1,
        to: 7,
        duration: 800,
        easing: am5.ease.out(am5.ease.cubic),
        loops: Infinity
    });
    circle.animate({
        key: "opacity",
        from: 1,
        to: 0,
        duration: 800,
        easing: am5.ease.out(am5.ease.cubic),
        loops: Infinity
    });
    document.getElementById("close").setAttribute("i", i);
    let name = document.createElement("p"), address = document.createElement("p"), lastMsg = document.createElement("p"), lastHs = document.createElement("p");
    name.innerText = "Name: " + NODES[i].name;
    address.innerText = "Address: " + NODES[i].address;
    lastMsg.innerText = "Last message: " + new Date(NODES[i].lastMessage).toLocaleString();
    lastHs.innerText = "Last handshake: " + new Date(NODES[i].lastHandshake).toLocaleString();
    document.getElementById("data").appendChild(name);
    document.getElementById("data").appendChild(address);
    document.getElementById("data").appendChild(lastMsg);
    document.getElementById("data").appendChild(lastHs);
    document.getElementById("inspect").style.display = "block";
    document.getElementById("default").style.display = "none";
}

var NODES = [];
var RAW = {
    all: [],
    connected: []
};
var addedIPs = [];
var CACHE = localStorage.getItem("cache") != null ? JSON.parse(localStorage.getItem("cache")) : [];

window.addEventListener("beforeunload", function() {
    localStorage.setItem("cache", JSON.stringify(CACHE));
}, false);

var updateWorker = new Worker("js/poll.js");//set new node --> poller.postMessage("213.239.193.208:9053");
var locatorWorker = new Worker("js/lookup.js");
updateWorker.onmessage = function(event) {

    let allIPs = RAW.all.map(x => x.address);
    for(let i = 0; i < event.data.all.length; i++) {
        if(!allIPs.includes(event.data.all[i].address)) {
            RAW.all[RAW.all.length] = event.data.all[i];
            RAW.all[RAW.all.length - 1].status = "off";
        }
    }

    let connectedIPs = RAW.connected.map(x => x.address);
    for(let i = 0; i < event.data.connected.length; i++) {
        if(!connectedIPs.includes(event.data.connected[i].address)) {
            RAW.connected[RAW.connected.length] = event.data.connected[i];
            RAW.connected[RAW.connected.length - 1].status = "on";
        }
    }

    let toProcess = [];
    for(let i = 0; i < RAW.connected.length; i++) {
        if(addedIPs.includes(RAW.connected[i].address)) {
            for(let j = 0; j < NODES.length; j++) if(NODES[j].address == RAW.connected[i].address) {
                NODES[j].lastMessage = RAW.connected[i].lastMessage;
                NODES[j].lastHandshake = RAW.connected[i].lastHandshake;
            }
        }else {
            let x = -1, c = CACHE.map(a => a.address);
            for(let j = 0; j < c.length; j++) if(c[j] == RAW.connected[i].address) {
                x = j;
                break;
            }
            if(x >= 0) {
                NODES[NODES.length] = {...RAW.connected[i], ...CACHE[x]};
                addNode(NODES.length - 1);
                addedIPs[addedIPs.length] = NODES[NODES.length - 1].address;
            }else {
                toProcess[toProcess.length] = RAW.connected[i];
            }
        }
    }
    locatorWorker.postMessage(toProcess);
};
locatorWorker.onmessage = function(event) {
    let shouldAdd = true;
    for(let i = 0; i < CACHE.length; i++) if(CACHE[i].address == event.data.address) shouldAdd = false;
    if(shouldAdd) {
        CACHE[CACHE.length] = {
            address: event.data.address,
            latitude: event.data.latitude,
            longitude: event.data.longitude,
            country: event.data.country,
            countryCode: event.data.countryCode,
            status: event.data.status
        };
    }
    if(!addedIPs.includes(event.data.address)) {
        NODES[NODES.length] = event.data;
        addNode(NODES.length - 1);
        addedIPs[addedIPs.length] = NODES[NODES.length - 1].address;
    }
};

for(let i = 0; i < NODES.length; i++) addNode(i);

chart.appear(1000, 100);

//  http://213.239.193.208:9053/peers/connected
//  http://213.239.193.208:9053/peers/all