var data = {
    all: [],
    connected: []
};

var url = "213.239.193.208:9053";

async function poll() {
    //all known nodes
    await fetch("http://" + url + "/peers/all")
    .then(res => res.json())
    .then(out => data.all = out)
    .catch(err => console.error(err));

    //connected nodes
    let raw;
    await fetch("http://" + url + "/peers/connected")
    .then(res => res.json())
    .then(out => raw = out)
    .catch(err => console.error(err));
    for(i in raw) {
        if(raw[i].address != "") {
            raw[i].address = raw[i].address.replace("/","").split(":")[0];
            data.connected[data.connected.length] = raw[i];
        }
    }

    //post
    postMessage(data);
    setTimeout("poll()", 60 * 1000);
}

poll();

self.addEventListener("message", function(event) {
    url = event.data;
}, false);