/*
	Copyright (c) 2014, Battelle Memorial Institute
    All rights reserved.
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
    1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
     
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
    The views and conclusions contained in the software and documentation are those
    of the authors and should not be interpreted as representing official policies,
    either expressed or implied, of the FreeBSD Project.
    This material was prepared as an account of work sponsored by an
    agency of the United States Government. Neither the United States
    Government nor the United States Department of Energy, nor Battelle,
    nor any of their employees, nor any jurisdiction or organization
    that has cooperated in the development of these materials, makes
    any warranty, express or implied, or assumes any legal liability
    or responsibility for the accuracy, completeness, or usefulness or
    any information, apparatus, product, software, or process disclosed,
    or represents that its use would not infringe privately owned rights.
    Reference herein to any specific commercial product, process, or
    service by trade name, trademark, manufacturer, or otherwise does
    not necessarily constitute or imply its endorsement, recommendation,
    or favoring by the United States Government or any agency thereof,
    or Battelle Memorial Institute. The views and opinions of authors
    expressed herein do not necessarily state or reflect those of the
    United States Government or any agency thereof.
    PACIFIC NORTHWEST NATIONAL LABORATORY
    operated by BATTELLE for the UNITED STATES DEPARTMENT OF ENERGY
    under Contract DE-AC05-76RL01830
*/
package pnnl.goss.gridpack.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.felix.ipojo.annotations.Component;
import org.fusesource.hawtbuf.ByteArrayInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import pnnl.goss.core.DataResponse;
import pnnl.goss.gridpack.common.datamodel.GridpackBus;
import pnnl.goss.gridpack.common.datamodel.GridpackPowergrid;
import pnnl.goss.gridpack.service.impl.GridpackUtils;
import pnnl.goss.powergrid.PowergridModel;
import pnnl.goss.powergrid.requests.RequestPowergrid;
import pnnl.goss.powergrid.server.PowergridService;
import pnnl.goss.powergrid.server.handlers.RequestPowergridHandler;

@Path("/")
@Component
public class GridpackServiceImpl {
	
	private static Logger log = LoggerFactory.getLogger(GridpackServiceImpl.class);
	
	public GridpackServiceImpl(){
		log.debug("DEFAULT CONSTRUCTOR");
	}
	
//	public GridpackServiceImpl(@Requires PowergridService powergridService){
//		log.debug("CONSTRUCTING USING PowergridService constructor");
//	}
	
	private PowergridService getPowergridService(){
		PowergridService service = null;
		try{
			InitialContext ic = new InitialContext();
			service = (PowergridService) ic.lookup("osgi:service/"+PowergridService.class.getName());
		}
		catch(NamingException e){
			log.error("Exception getting: " + PowergridService.class.getName()+ "\n"+e.getMessage());
		}
		return service;
	}
	
	@GET
	@Path("/{powergridName}/buses/{numberOfBuses}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Collection<GridpackBus> getBuses0ToN(
			@PathParam(value = "powergridName") String powergridName, 
			@PathParam(value = "numberOfBuses") int numberOfBuses){
		
		return getBusesNToM(powergridName, 0, numberOfBuses);
	}
	
	@GET
	@Path("/{powergridName}/buses/{startAtIndex}/{numberOfBuses}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Collection<GridpackBus> getBusesNToM(
			@PathParam(value = "powergridName") String powergridName, 
			@PathParam(value = "startAtIndex") int startAtIndex,
			@PathParam(value = "numberOfBuses") int numberOfBuses){
		
		GridpackPowergrid grid = getGridpackGrid(powergridName);
		List<GridpackBus> buses = new ArrayList<GridpackBus>(grid.getBuses());
		
		if (buses.size() > startAtIndex + numberOfBuses){
			return buses.subList(startAtIndex, startAtIndex+numberOfBuses);
		}
		else if(buses.size() > startAtIndex){
			return buses.subList(startAtIndex, buses.size());
		}
				
		return null;
	}
	
	@GET
    @Path("/{powergridName}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public GridpackPowergrid getGridpackGrid(
			@PathParam(value = "powergridName") String powergridName){
		
		GridpackPowergrid pg = null;
		
		PowergridModel powergrid = getPowergridService().getPowergridModel(powergridName);
		
		if (powergrid == null){
			// Make sure the response didn't throw an error.
			GridpackUtils.throwInputError("Invalid powergrid specified: "+powergridName);
			return null;
		}
					
		pg = new GridpackPowergrid(powergrid);
		
		return pg;
	}
	
	@GET
    @Path("/{powergridName}/full")
	@Produces({MediaType.TEXT_PLAIN})
	public String getGridpackGridWithWadl(
			@PathParam(value = "powergridName") String powergridName, 
			@PathParam(value="plain") String asPlain){
		
		GridpackPowergrid pg = null;
		
		PowergridModel powergrid = getPowergridService().getPowergridModel(powergridName);
		
		if (powergrid == null){
			// Make sure the response didn't throw an error.
			GridpackUtils.throwInputError("Invalid powergrid specified: "+powergridName);
			return null;
		}
					
		pg = new GridpackPowergrid(powergrid);
		
		URL url;
	    InputStream is = null;
	    BufferedReader br;
	    String line;
	    StringWriter schemaWriter = new StringWriter();
	    StringBuffer buf = new StringBuffer();
	    StringWriter pretty = new StringWriter();

	    try {
	        url = new URL("http://localhost:8181/cxf/gridpack?_wadl");
	        is = url.openStream();  // throws an IOException
	        br = new BufferedReader(new InputStreamReader(is));

	        while ((line = br.readLine()) != null) {
	        	schemaWriter.write(line);
	        }
	    } catch (MalformedURLException mue) {
	         mue.printStackTrace();
	    } catch (IOException ioe) {
	         ioe.printStackTrace();
	    } finally {
	        try {
	            if (is != null) is.close();
	        } catch (IOException ioe) {
	            // nothing to see here
	        }
	    }
	    
	    try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputStream input = new ByteArrayInputStream(schemaWriter.toString().getBytes());
			Document document = builder.parse(input);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			serialize(document, output);
			buf.append(output.toString());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    JAXBContext jaxbCtx = null;
        StringWriter xmlWriter = null;
        try {
            //XML Binding code using JAXB
         
            jaxbCtx = JAXBContext.newInstance(GridpackPowergrid.class);
            xmlWriter = new StringWriter();
            Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(pg, xmlWriter);
            //System.out.println("XML Marshal example in Java");
            //System.out.println(xmlWriter);
         
//            Booking b = (Booking) jaxbCtx.createUnmarshaller().unmarshal(
//                                               new StringReader(xmlWriter.toString()));
//            System.out.println("XML Unmarshal example in JAva");
//            System.out.println(b.toString());
        } catch (JAXBException ex) {
        	log.error("Xml exception" , ex);
//            Logger.getLogger(JAXBXmlBindExample.class.getName()).log(Level.SEVERE,
//                                                                          null, ex);
        }
        
        
        //buf.append(schemaWriter.toString());
        buf.append(xmlWriter.toString());	    
		
		
		return buf.toString();
	}
	
	
	
	@GET
	@Path("/{powergridName}/bus/count")
	@Produces(MediaType.TEXT_PLAIN)
	public Integer getNumberOfBuses(
			@PathParam(value = "powergridName") String powergridName)
	{
		RequestPowergrid request = new RequestPowergrid(powergridName);
		RequestPowergridHandler handler = new RequestPowergridHandler();
		DataResponse response = handler.getResponse(request);
		
		// Make sure the response didn't throw an error.
		GridpackUtils.throwDataError(response);
		
		PowergridModel model = (PowergridModel)response.getData();
		
		return model.getBuses().size();
	}
	
	@GET
	@Path("/{powergridName}/branch/count")
	@Produces(MediaType.TEXT_PLAIN)
	public Integer getNumberOfBranches(
			@PathParam(value = "powergridName") String powergridName)
	{
		RequestPowergrid request = new RequestPowergrid(powergridName);
		RequestPowergridHandler handler = new RequestPowergridHandler();
		DataResponse response = handler.getResponse(request);
		
		// Make sure the response didn't throw an error.
		GridpackUtils.throwDataError(response);
		
		PowergridModel model = (PowergridModel)response.getData();
		
		return model.getBranches().size();
	}
	
//	@GET
//	@Path("/{powergridName}/branches/{numberOfBranches}")
//	@Produces({MediaType.APPLICATION_JSON})
//	public Collection<GridpackBranch> getBranches0ToN(
//			@PathParam(value = "powergridName") String powergridName, 
//			@PathParam(value = "numberOfBranches") int numberOfBranches){
//		
//		Collection<GridpackBranch> branches = getBranchesNToM(powergridName, 0, numberOfBranches);
//		return branches;
//	}
	
//	@GET
//	@Path("/{powergridName}/branches/{startAtIndex}/{numberOfBranches}")
//	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//	public Collection<GridpackBranch> getBranchesNToM(
//			@PathParam(value = "powergridName") String powergridName, 
//			@PathParam(value = "startAtIndex") int startAtIndex,
//			@PathParam(value = "numberOfBranches") int numberOfBranches){
//		
//		GridpackPowergrid grid = getGridpackGrid(powergridName);
//		List<GridpackBranch> branches = new ArrayList<GridpackBranch>(grid.getBranches());
//		
//		if (branches.size() > startAtIndex + numberOfBranches){
//			return branches.subList(startAtIndex, startAtIndex+numberOfBranches);
//		}
//		else if(branches.size() > startAtIndex){
//			return branches.subList(startAtIndex, branches.size());
//		}
//		
//		return null;
//	}
	
	

//	public Collection<GridpackBus> getBusesTimesteps(String powergridId,
//			String timestep) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	 public void serialize(Document doc, OutputStream out) throws Exception {

		  TransformerFactory tfactory = TransformerFactory.newInstance();
		  Transformer serializer;
		  try {
		   serializer = tfactory.newTransformer();
		   //Setup indenting to "pretty print"
		   serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		   serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		   DOMSource xmlSource = new DOMSource(doc);
		   StreamResult outputTarget = new StreamResult(out);
		   serializer.transform(xmlSource, outputTarget);
		  } catch (TransformerException e) {
		   // this is fatal, just dump the stack and throw a runtime exception
		   e.printStackTrace();

		   throw new RuntimeException(e);
		  }
		 }
		
}
