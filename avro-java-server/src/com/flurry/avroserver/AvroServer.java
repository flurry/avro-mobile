package com.flurry.avroserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.StringUtil;

import com.flurry.avroserver.protocol.v1.Ad;
import com.flurry.avroserver.protocol.v1.AdRequest;
import com.flurry.avroserver.protocol.v1.AdResponse;

/**
 * This lightweight avro server provides a web interface for remote
 * applications. Intended to illustrate receiving and parsing an Avro request
 * followed by constructing and encoding an Avro response. Supports both Avro
 * binary and JSON.
 * 
 * @author anthony@flurry
 */
public class AvroServer {

	private static Server server = null;
	protected static DecoderFactory DECODER_FACTORY = new DecoderFactory();
	protected static final String JSON_CONTENT_TYPE = "application/json";
	protected static final String BINARY_CONTENT_TYPE = "avro/binary";

	private AvroServer() {
	}

	public static void main(String[] args) {
		try {
			if (args.length == 1) {
				int httpport;

				try {
					httpport = Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(
							"Must provide valid http port.");
				}

				AvroServer avroServer = new AvroServer();

				server = new Server(httpport);

				ContextHandlerCollection contexts = new ContextHandlerCollection();

				ContextHandler context = new ContextHandler();
				context.setContextPath("/");
				context.setResourceBase(".");
				context.setClassLoader(Thread.currentThread()
						.getContextClassLoader());
				context.setHandler(new WebHandler(avroServer));
				contexts.addHandler(context);

				server.setHandler(contexts);

				server.setGracefulShutdown(1000);
				server.setStopAtShutdown(true);

				server.start(); // spawns a new thread.
				server.join();
			} else {
				System.out.println("usage: java AvroServer <http port>");
			}
		} catch (Exception e) {
			System.err.println("Catastrophic failure " + e.getMessage());
			System.exit(1);
		}
		System.exit(0);
	}

	protected AdRequest readRequest(InputStream is, String contentType)
			throws IOException {
		AdRequest adRequest = null;

		Decoder decoder = null;
		if (contentType == null) {
			throw new IOException("Content type not set for Ad Request");
		}

		if (contentType.equals(BINARY_CONTENT_TYPE)) {
			decoder = DECODER_FACTORY.binaryDecoder(is, null);
		} else if (contentType.equals(JSON_CONTENT_TYPE)) {
			decoder = DECODER_FACTORY.jsonDecoder(AdRequest.SCHEMA$, is);
		} else {
			throw new IOException("Unknown content type for Ad Request");

		}

		SpecificDatumReader<AdRequest> reader = new SpecificDatumReader<AdRequest>(
				AdRequest.class);

		try {
			// Should only be 1 request object
			adRequest = reader.read(null, decoder);

			System.out.println("Read Request: " + adRequest);
			return adRequest;

		} catch (EOFException eof) {
			System.err.println("End of file: " + eof.getMessage());
		} catch (Exception ex) {
			System.err
					.println("Error in processing request " + ex.getMessage());
		}

		return adRequest;
	}

	protected byte[] getResponse(AdRequest request, String returnFormat)
			throws IOException {
		// Demonstrate ad generation or error output
		AdResponse adResponse = null;

		if (request.getAdSpaceName().toString().equalsIgnoreCase("throwError")) {
			List<CharSequence> errors = new ArrayList<CharSequence>();
			errors.add((CharSequence) "Ad server has exploded.");
			adResponse = AdResponse.newBuilder().setErrors(errors).setAds(new ArrayList<Ad>()).build();
		} else {
			// Generate a couple of sample ads
			List<Ad> ads = new ArrayList<Ad>();

			// Get Ad Space name from request and just make up a name of an Ad
			Ad ad1 = Ad.newBuilder().setAdSpace(request.getAdSpaceName())
					.setAdName("Awesome App").build();
			Ad ad2 = Ad.newBuilder().setAdSpace(request.getAdSpaceName())
					.setAdName("Even better App").build();
			ads.add(ad1);
			ads.add(ad2);

			adResponse = AdResponse.newBuilder().setAds(ads).build();
		}

		SpecificDatumWriter<AdResponse> writer = new SpecificDatumWriter<AdResponse>(
				AdResponse.class);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		if (returnFormat.equals(BINARY_CONTENT_TYPE)) {
			System.out.println("Returning binary");

			BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out,
					null);
			writer.write(adResponse, encoder);
			encoder.flush();
			ByteBuffer serialized = ByteBuffer
					.allocate(out.toByteArray().length);
			serialized.put(out.toByteArray());

			return serialized.array();
		} else {
			System.out.println("Returning json");

			JsonEncoder jsonEncoder = EncoderFactory.get().jsonEncoder(
					AdResponse.SCHEMA$, out);
			writer.write(adResponse, jsonEncoder);
			jsonEncoder.flush();
			return out.toByteArray();

		}

	}

	static class WebHandler extends AbstractHandler {

		private AvroServer avroServer;

		public WebHandler(AvroServer avroServer) {
			this.avroServer = avroServer;
		}

		@Override
		public void handle(String target, Request baseRequest,
				HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			response.setStatus(HttpServletResponse.SC_OK);

			final String url = request.getRequestURI();

			try {
				System.out.println("URL: " + url + " Method: "
						+ request.getMethod());

				// Read Avro request
				AdRequest adRequest = avroServer.readRequest(
						request.getInputStream(), request.getContentType());

				// First determined return format. If accept header is specified
				// use that, else refer to the content type
				String acceptHeader = request.getHeader("accept");
				String contentType = request.getContentType();
				String returnFormat = acceptHeader != null
						&& (acceptHeader.equals(BINARY_CONTENT_TYPE) || acceptHeader
								.equals(JSON_CONTENT_TYPE)) ? acceptHeader
						: contentType;

				// Process request object to get response
				byte[] adResponse = avroServer.getResponse(adRequest,
						returnFormat);

				if (adResponse != null)
				{
					response.setContentLength(adResponse.length);
					response.setContentType(returnFormat);
					response.getOutputStream().write(adResponse);
				}
			} catch (IllegalArgumentException e) {
				System.err.println(e.getMessage());
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
			((Request) request).setHandled(true);
		}
	}
}
