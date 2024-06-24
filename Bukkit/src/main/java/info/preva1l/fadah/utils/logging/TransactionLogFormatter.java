package info.preva1l.fadah.utils.logging;

import info.preva1l.fadah.utils.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class TransactionLogFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        String formattedTime = new SimpleDateFormat("HH:mm:ss").format(Date.from(Instant.now()));
        String message = formatMessage(record);
        String throwable = "";
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }
        return StringUtils.formatPlaceholders("[{0} {1}] {2}\n{3}",
                formattedTime,
                record.getLevel().getLocalizedName(),
                message,
                throwable);
    }
}
