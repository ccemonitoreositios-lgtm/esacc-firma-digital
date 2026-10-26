package ec.gob.cce.firmadigital.global;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/firmadigitaltest")
public class FirmaDigitalResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response test() {
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Servicio Firma Digital activo");
        return Response.ok(response).build();
    }
}
