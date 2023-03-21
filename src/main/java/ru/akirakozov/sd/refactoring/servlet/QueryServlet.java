package ru.akirakozov.sd.refactoring.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author akirakozov
 */
public class QueryServlet extends HttpServlet {
    private enum Command {
        MIN, MAX, SUM, COUNT, UNKNOWN(null);

        Command() {
            this.unparsedCommand = null;
        }

        Command(String unparsedCommand) {
            this.unparsedCommand = unparsedCommand;
        }

        private final String unparsedCommand;

        public static Command getFromRequest(String command) {
            if (command == null) {
                return UNKNOWN;
            }
            switch (command) {
                case "min": return MIN;
                case "max": return MAX;
                case "sum": return SUM;
                case "count": return COUNT;
                default: return UNKNOWN;
            }
        }
    }

    private static EnumMap<Command, String> cmdToSql = new EnumMap<Command, String>(Map.of(
        Command.MAX, "SELECT * FROM PRODUCT ORDER BY PRICE DESC LIMIT 1",
        Command.MIN, "SELECT * FROM PRODUCT ORDER BY PRICE LIMIT 1",
        Command.SUM, "SELECT SUM(price) FROM PRODUCT",
        Command.COUNT, "SELECT COUNT(*) FROM PRODUCT"
    ));

    private static final EnumMap<Command, String> cmdToHeader = new EnumMap<Command, String>(Map.of(
        Command.MAX, "<h1>Product with max price: </h1>",
        Command.MIN, "<h1>Product with min price: </h1>",
        Command.SUM, "Summary price: ",
        Command.COUNT, "Number of products: "
    ));

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String commandStr = request.getParameter("command");
        Command cmd = Command.getFromRequest(commandStr);

        if (cmd == Command.UNKNOWN) {
            response.getWriter().printf("Unknown command: %s", commandStr);
            finishResponse(response);
            return;
        }

        try {
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
                Statement stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery(cmdToSql.get(cmd));

                response.getWriter().println("<html><body>");
                response.getWriter().println(cmdToHeader.get(cmd));

                switch (cmd) {
                    case MIN:
                    case MAX: {
                        while (rs.next()) {
                            String name = rs.getString("name");
                            int price = rs.getInt("price");
                            response.getWriter().printf("%s\t%s</br>\n", name, price);
                        }
                        break;
                    }
                    case SUM:
                    case COUNT: {
                        if (rs.next()) {
                            int price = rs.getInt(1);
                            response.getWriter().println(price);
                        }
                        break;
                    }
                    default: throw new IllegalArgumentException(cmd.name());
                }
                response.getWriter().println("</body></html>");

                rs.close();
                stmt.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        finishResponse(response);
    }

    private static void finishResponse(HttpServletResponse response) {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
