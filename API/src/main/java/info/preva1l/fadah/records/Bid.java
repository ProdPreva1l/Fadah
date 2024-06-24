package info.preva1l.fadah.records;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public record Bid(
        @Expose
        UUID bidder,
        @Expose
        @SerializedName("bid_amount")
        double bidAmount,
        @Expose
        long timestamp
) {
}
