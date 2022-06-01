var dataIn = [];
var dataOut = {};

async function get() {
    if(dataIn.length > 0) {
        dataOut = dataIn.splice(0, 1)[0];
        await fetch("http://ip-api.com/json/" + dataOut.address + "?fields=country,countryCode,lat,lon")
        .then(res => res.json())
        .then(out => {
            dataOut.latitude = out.lat;
            dataOut.longitude = out.lon;
            dataOut.country = out.country;
            dataOut.countryCode = out.countryCode;
        })
        .catch(err => console.error(err));
        if(dataOut.countryCode != undefined) postMessage(dataOut);
    }
    setTimeout("get()", 1500);
}

self.addEventListener("message", function(event) {
    dataIn = dataIn.concat(event.data);
}, false);

get();