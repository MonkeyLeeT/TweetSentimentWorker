import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.amazonaws.auth.PropertiesCredentials;
import com.fasterxml.jackson.core.JsonParseException;
import com.google.gson.Gson;

/**
 * Servlet implementation class process
 */
public class process extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Gson gson = new Gson();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public process() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
			PropertiesCredentials propertiesCredentials = new PropertiesCredentials(Thread.currentThread().getContextClassLoader().getResourceAsStream("credentials.ini"));
			SNS sns = new SNS(propertiesCredentials);
        	TweetRequest tweetRequest = TweetRequest.fromJson(request.getInputStream());
        	String sentiment = getSentiment(tweetRequest.getText());
        	SentimentResult sentimentResult = gson.fromJson(sentiment, SentimentResult.class);
        	double sentimentValue = Math.random() * 2 - 1;
        	
        	if (sentimentResult.status.equals("OK"))
        		sentimentValue = sentimentResult.docSentiment.score;
        	else
        		System.out.println("Failed to get sentiment." + sentimentResult.statusInfo);	
        	
    		getRds().update(tweetRequest.getId_str(), sentimentValue);
			tweetRequest.setSentiment(sentimentValue);
			System.out.println("Send to sns: " + tweetRequest.getText());
			sns.publish(gson.toJson(tweetRequest));
			response.setStatus(200);
        } catch (JsonParseException e) {
        	System.err.println("Error in parsing json string.");
            e.printStackTrace(System.out);
			response.setStatus(201);
        } catch (RuntimeException exception) {
                exception.printStackTrace(System.out);
    			response.setStatus(500);
    			}
        }
	
	private String getSentiment(String text) throws ClientProtocolException, IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://access.alchemyapi.com/calls/text/TextGetTextSentiment");
        List<NameValuePair> params = new ArrayList<NameValuePair>(3);
        String apiKey = getApiKey();
        
        params.add(new BasicNameValuePair("apikey", apiKey));
        params.add(new BasicNameValuePair("text", text));
        params.add(new BasicNameValuePair("outputMode", "json"));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();
        String ret = null;
        if (entity != null) {
            InputStream instream = entity.getContent();
            try {
                ret = EntityUtils.toString(entity);
            } finally {
                instream.close();
            }
        }
        return ret;
	}
	
	@SuppressWarnings("resource")
	private String getApiKey() {
		InputStream password = Thread.currentThread().getContextClassLoader().getResourceAsStream("apikey.ini");
        String pass = null;
        pass = new Scanner(password).next();
        return pass;
	}
	
	@SuppressWarnings("resource")
	private String readPass() {
		InputStream password = Thread.currentThread().getContextClassLoader().getResourceAsStream("credentials.ini");
        String pass = null;
        pass = new Scanner(password).next();
        return pass;
	}
	
	private Rds getRds() {
		Rds rds = Rds.getInstance();
		if (!rds.isPasswordSet())
			rds.setPassword(readPass());
		return rds;
	}

}
