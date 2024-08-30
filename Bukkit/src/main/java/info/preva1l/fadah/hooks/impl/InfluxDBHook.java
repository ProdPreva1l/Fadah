package info.preva1l.fadah.hooks.impl;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.hooks.Hook;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class InfluxDBHook implements Hook {
    private boolean enabled;

    private final InfluxDBClient client;
    private final WriteApiBlocking writeApi;

    public InfluxDBHook() {
        try {
            String url = Config.HOOK_INFLUX_URI.toString();
            String token = Config.HOOK_INFLUX_TOKEN.toString();
            String org = Config.HOOK_INFLUX_ORG.toString();
            String bucket = Config.HOOK_INFLUX_BUCKET.toString();
            this.client = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
            this.writeApi = client.getWriteApiBlocking();
            this.enabled = true;
        } catch (Exception e) {
            this.enabled = false;
            throw new RuntimeException(e);
        }
    }

    public void log(String message) {
        Point point = Point.measurement("Fadah")
                .time(Instant.now(), WritePrecision.MS)
                .addField("message", message);
        writeApi.writePoint(point);
    }

    public void destroy() {
        if (client == null) return;
        client.close();
    }
}
