package com.legacy.catalog.servlet;

import com.legacy.catalog.model.Product;
import com.legacy.catalog.service.ProductCatalogService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class SoapServlet extends HttpServlet {

    private ProductCatalogService service = new ProductCatalogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String query = req.getQueryString();
        if (query != null && query.contains("wsdl")) {
            resp.setContentType("text/xml");
            PrintWriter out = resp.getWriter();
            out.println(generateWsdl(req));
            return;
        }

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");
        out.println("<h1>ProductCatalogService</h1>");
        out.println("<p>Legacy SOAP Service - Version " + ProductCatalogService.SERVICE_VERSION + "</p>");
        out.println("<p>Call count: " + ProductCatalogService.getCallCount() + "</p>");
        out.println("<h2>Available Operations:</h2><ul>");
        out.println("<li>getAllProducts</li>");
        out.println("<li>getProduct (id)</li>");
        out.println("<li>createProduct (name, price, category, stock)</li>");
        out.println("<li>updateProduct (id, name, price, category, stock)</li>");
        out.println("<li>deleteProduct (id)</li>");
        out.println("<li>searchProducts (keyword)</li>");
        out.println("<li>getInventoryReport</li>");
        out.println("</ul>");
        out.println("<p><a href='" + req.getRequestURL() + "?wsdl'>WSDL</a></p>");
        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("========================================");
        System.out.println("[SOAP] Incoming request from: " + req.getRemoteAddr());
        System.out.println("========================================");

        String body = readBody(req);
        System.out.println("[SOAP] Request body:\n" + body);

        String operation = extractOperation(body);
        System.out.println("[SOAP] Detected operation: " + operation);

        String responseXml;

        switch (operation) {
            case "getAllProducts":
                responseXml = handleGetAllProducts();
                break;
            case "getProduct":
                String id = extractParam(body, "id");
                responseXml = handleGetProduct(id);
                break;
            case "createProduct":
                responseXml = handleCreateProduct(body);
                break;
            case "updateProduct":
                responseXml = handleUpdateProduct(body);
                break;
            case "deleteProduct":
                String delId = extractParam(body, "id");
                responseXml = handleDeleteProduct(delId);
                break;
            case "searchProducts":
                String keyword = extractParam(body, "keyword");
                responseXml = handleSearchProducts(keyword);
                break;
            case "getInventoryReport":
                responseXml = handleGetInventoryReport();
                break;
            default:
                responseXml = buildFault("UnknownOperation", "Operation not recognized: " + operation);
        }

        resp.setContentType("text/xml; charset=utf-8");
        resp.setStatus(200);
        PrintWriter out = resp.getWriter();
        out.println(responseXml);
        System.out.println("[SOAP] Response sent.");
    }

    private String handleGetAllProducts() {
        Product[] products = service.getAllProducts();
        StringBuilder sb = new StringBuilder();
        sb.append("<ns:getAllProductsResponse xmlns:ns=\"http://service.catalog.legacy.com\">");
        for (Product p : products) {
            sb.append(productToXml(p));
        }
        sb.append("</ns:getAllProductsResponse>");
        return wrapEnvelope(sb.toString());
    }

    private String handleGetProduct(String id) {
        Product p = service.getProduct(id);
        StringBuilder sb = new StringBuilder();
        sb.append("<ns:getProductResponse xmlns:ns=\"http://service.catalog.legacy.com\">");
        sb.append(productToXml(p));
        sb.append("</ns:getProductResponse>");
        return wrapEnvelope(sb.toString());
    }

    private String handleCreateProduct(String body) {
        String name = extractParam(body, "name");
        String price = extractParam(body, "price");
        String category = extractParam(body, "category");
        String stock = extractParam(body, "stock");
        String result = service.createProduct(name, price, category, stock);
        return wrapEnvelope("<ns:createProductResponse xmlns:ns=\"http://service.catalog.legacy.com\">"
                + "<return>" + escapeXml(result) + "</return>"
                + "</ns:createProductResponse>");
    }

    private String handleUpdateProduct(String body) {
        String id = extractParam(body, "id");
        String name = extractParam(body, "name");
        String price = extractParam(body, "price");
        String category = extractParam(body, "category");
        String stock = extractParam(body, "stock");
        String result = service.updateProduct(id, name, price, category, stock);
        return wrapEnvelope("<ns:updateProductResponse xmlns:ns=\"http://service.catalog.legacy.com\">"
                + "<return>" + escapeXml(result) + "</return>"
                + "</ns:updateProductResponse>");
    }

    private String handleDeleteProduct(String id) {
        String result = service.deleteProduct(id);
        return wrapEnvelope("<ns:deleteProductResponse xmlns:ns=\"http://service.catalog.legacy.com\">"
                + "<return>" + escapeXml(result) + "</return>"
                + "</ns:deleteProductResponse>");
    }

    private String handleSearchProducts(String keyword) {
        Product[] products = service.searchProducts(keyword);
        StringBuilder sb = new StringBuilder();
        sb.append("<ns:searchProductsResponse xmlns:ns=\"http://service.catalog.legacy.com\">");
        for (Product p : products) {
            sb.append(productToXml(p));
        }
        sb.append("</ns:searchProductsResponse>");
        return wrapEnvelope(sb.toString());
    }

    private String handleGetInventoryReport() {
        String report = service.getInventoryReport();
        return wrapEnvelope("<ns:getInventoryReportResponse xmlns:ns=\"http://service.catalog.legacy.com\">"
                + "<return>" + escapeXml(report) + "</return>"
                + "</ns:getInventoryReportResponse>");
    }

    // ==================== UGLY HELPER METHODS ====================

    private String productToXml(Product p) {
        return "<product>"
                + "<id>" + escapeXml(p.id) + "</id>"
                + "<name>" + escapeXml(p.name) + "</name>"
                + "<price>" + escapeXml(p.price) + "</price>"
                + "<category>" + escapeXml(p.category) + "</category>"
                + "<stock>" + escapeXml(p.stock) + "</stock>"
                + "</product>";
    }

    private String wrapEnvelope(String body) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<soapenv:Body>"
                + body
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";
    }

    private String buildFault(String code, String message) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<soapenv:Body>"
                + "<soapenv:Fault>"
                + "<faultcode>" + code + "</faultcode>"
                + "<faultstring>" + escapeXml(message) + "</faultstring>"
                + "</soapenv:Fault>"
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";
    }

    private String extractOperation(String xml) {
        // Horrible XML parsing - just find the first element inside Body
        String[] operations = {"getAllProducts", "getProduct", "createProduct",
                "updateProduct", "deleteProduct", "searchProducts", "getInventoryReport"};
        for (String op : operations) {
            if (xml.contains(op)) return op;
        }
        return "unknown";
    }

    private String extractParam(String xml, String param) {
        // Terrible way to parse XML - regex on XML (anti-pattern!)
        String open = "<" + param + ">";
        String close = "</" + param + ">";
        int start = xml.indexOf(open);
        if (start == -1) {
            // Try with namespace prefix
            for (String prefix : new String[]{"ser:", "ns:", "cat:"}) {
                open = "<" + prefix + param + ">";
                close = "</" + prefix + param + ">";
                start = xml.indexOf(open);
                if (start != -1) break;
            }
            if (start == -1) return "";
        }
        start += open.length();
        int end = xml.indexOf(close, start);
        if (end == -1) return "";
        return xml.substring(start, end).trim();
    }

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String generateWsdl(HttpServletRequest req) {
        String baseUrl = req.getRequestURL().toString().replace("?wsdl", "");
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<definitions name=\"ProductCatalogService\"\n"
                + "  xmlns=\"http://schemas.xmlsoap.org/wsdl/\"\n"
                + "  xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"\n"
                + "  xmlns:tns=\"http://service.catalog.legacy.com\"\n"
                + "  targetNamespace=\"http://service.catalog.legacy.com\">\n"
                + "  <service name=\"ProductCatalogService\">\n"
                + "    <port name=\"ProductCatalogPort\" binding=\"tns:ProductCatalogBinding\">\n"
                + "      <soap:address location=\"" + baseUrl + "\"/>\n"
                + "    </port>\n"
                + "  </service>\n"
                + "</definitions>";
    }
}
