package customer.external_api.handlerss;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;

import cds.gen.externalapiservice.ExternalApiService_;
import cds.gen.externalapiservice.GetdataforuserIdContext;

@Component
@ServiceName(value = ExternalApiService_.CDS_NAME)
public class externalapihandler implements EventHandler {

    private static final Logger  LOG= LoggerFactory.getLogger(externalapihandler.class);
    


    @On(event = GetdataforuserIdContext.CDS_NAME)
    public void getDatasForUserId(GetdataforuserIdContext context) throws IOException, InterruptedException
    {
        LOG.info("externalapihandler is being called");

            // Get your configured BTP destination
            HttpDestination destination =
                    DestinationAccessor
                            .getDestination("demoAPI")
                            .asHttp();

            // Create an HTTP client configured for this destination
            HttpClient httpClient = HttpClient.newBuilder().build();
 
                String baseUrl = destination.getUri()
                        .toString()
                        .replaceAll("/+$", "");

                URI uri = URI.create(baseUrl+"/users");

                HttpRequest request = HttpRequest.newBuilder(uri).header("Accept", "application/json").GET().build();

              HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
               
                int status = response.statusCode();
                    if (status < 200 || status >= 300) {
                        throw new ServiceException(
                                ErrorStatuses.SERVER_ERROR,
                                "External API returned HTTP " + status
                        );
                    }

                    context.setResult(response.body().toString());
    }
}
                

              
    
