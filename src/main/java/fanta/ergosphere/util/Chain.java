package fanta.ergosphere.util;

import fanta.ergosphere.process.Node;
import static fanta.ergosphere.util.General.getDate;
import static fanta.ergosphere.util.General.jo;
import java.util.Arrays;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class Chain {

    public static final class Box {
        public final String tx, address, id;
        public final long time, value;
        public final HashMap<String,Long> assets = new HashMap<String,Long>();
        public final boolean spent;
        public Box(final Object o) {
            final JSONObject jo = jo(o);
            tx = jo.getString("creationTransaction");
            address = jo.getString("address");
            id = jo.getJSONObject("box").getString("boxId");
            time = getDate(jo.getLong("inclusionHeightS"));
            value = jo.getJSONObject("box").getLong("value");
            final JSONArray ja = jo.getJSONArray("assets");
            if(!ja.isEmpty()) ja.forEach(x -> assets.put(jo(x).getString("tokenId"), jo(x).getLong("amount")));
            spent = jo.getBoolean("spent");
        }
    }

    public static final class Tx {
        public final String address;
        public final long value, time;
        public Tx(Box[] boxes) {
            address = boxes[0].address;
            final long in  = Arrays.stream(boxes).filter(x ->!x.spent).mapToLong(y -> y.value).sum();
            final long out = Arrays.stream(boxes).filter(x -> x.spent).mapToLong(y -> y.value).sum();
            value = in - out;
            time = boxes[0].time;
        }
        public JSONObject toJSON() {
            return new JSONObject().put("address", address).put("value", value).put("time", time);
        }
    }

    public static final class Block {
        public final int txs;
        public final long num, time;
        public final boolean full;
        public Block(final Object o, final boolean isFullBlock) {
            JSONObject jo = new JSONObject().put("blockTransactions", new JSONObject().put("transactions", new JSONArray())).put("header", new JSONObject().put("height", 0).put("timestamp", 0L));
            try {
                JSONObject tmp = General.getJSONObject(Node.LOCAL + "/blocks/" + (isFullBlock ? "" : "modifier/") + ((String) o));
                if(isFullBlock) jo = tmp; else jo.put("header", tmp);
            }catch(JSONException e) {}
            txs = isFullBlock ? jo.getJSONObject("blockTransactions").getJSONArray("transactions").length() : 0;
            num = jo.getJSONObject("header").optInt("height");
            time = jo.getJSONObject("header").optLong("timestamp");
            full = isFullBlock;
        }
        public JSONObject toJSON() {
            return new JSONObject().put("txs", txs).put("num", num).put("time", time).put("full", full);
        }
    }
}