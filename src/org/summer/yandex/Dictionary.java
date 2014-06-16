package org.summer.yandex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;

public class Dictionary {
	
	private Queue<JSONObject> readQ = new LinkedList<JSONObject>();
	private Queue<JSONObject> writeQ = new LinkedList<JSONObject>();
	private Boolean reading = true;
	private Boolean changing = true;
	private BufferedReader br = null;
	private FileWriter fw = null;
	private Thread read = new Thread();
	private Thread change = new Thread();
	private Thread write = new Thread();
	
    public static void main( String[] args ) {
		new Dictionary().start();
    }

    private void start() {
    	read = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
			    	try {
			    		br = new BufferedReader(new FileReader("intern-task.json.filtered"));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			    	
			    	String line;
			    	while ((line = br.readLine()) !=null ) {

			    		JSONObject obj = (JSONObject)new JSONParser().parse(line);
			  	
			    		synchronized (readQ) {
			    			readQ.add(obj);
			    		}
			    		
			    		if (change.getState() == Thread.State.WAITING) {
			    			synchronized (change) {
			    				change.notify();
			    			}
			    		}
	   		
		    		}
			    	br.close();
			    	
			    	reading = false;
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
    		
    	});

    	change = new Thread(new Runnable() {

			@Override
			public void run() {
				while (reading | !readQ.isEmpty()) {
					JSONObject obj;
					synchronized (readQ) {
						obj = readQ.poll();
					}
					if (obj == null) {
						synchronized (this) {
							try {
								this.wait(50);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					} else {
		    			String content = (String) obj.get("content");
		    			String title = (String) obj.get("title");
		    			content  = Jsoup.parse(content).text();
		    			title = Jsoup.parse(title).text();

		    			String snippet = contentToSnippet(content, title);
		    		
		    			obj.remove("content");
		    			obj.remove("title");
		    			obj.put("snippet", snippet);
		    		
		    			synchronized (writeQ) {
		    				writeQ.add(obj);
		    			}
		    			
		    			if (write.getState() == Thread.State.WAITING) {
		    				synchronized (write) {
		    					write.notify();
		    				}
		    			}
					}
				}
				changing = false;
			}
    		
    	});
    	   	
    	write = new Thread(new Runnable() {
			@Override
			public void run() {
		    	try {
					fw = new FileWriter("result.json.filtered");
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				while (changing | !writeQ.isEmpty()) {
					JSONObject obj;
					synchronized (writeQ) {
						obj = writeQ.poll();
					}
					if (obj == null) {
						synchronized (this) {
							try {
								this.wait(50);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					} else {
		    			String snippet = (String) obj.get("snippet");
		    			String url = (String) obj.get("url");

		    			String text = "{\"url\": \""+url+"\", \"snippet\": \""+snippet+"\"}\n";
		    			
		    			try {
							fw.write(text);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				try {
					fw.flush();
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
    	});
    	
    	read.start();
    	change.start();
    	write.start();
    	
    	try {
			read.join();
	    	change.join();
	    	write.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
/*    
    private void start() throws ParseException, IOException {
    	BufferedReader br = new BufferedReader(new FileReader("intern-task.json.filtered"));
    	FileWriter fw = new FileWriter("result.json.filtered");

    	String line;
    	while ((line = br.readLine()) !=null ) {

    		JSONObject obj = (JSONObject)new JSONParser().parse(line);
  	
    		String content = (String) obj.get("content");
    		String title = (String) obj.get("title");
    		content  = Jsoup.parse(content).text();
    		title = Jsoup.parse(title).text();
    		String snippet = contentToSnippet(content, title);
    		
    		fw.write("{\"url\": \""+obj.get("url")+"\", \"snippet\": \""+snippet+"\"}\n");
   		}
		br.close();		

		fw.close();
    }
*/    
	private String contentToSnippet(String content, String title) {
		if (content.length() > 300) {
			content = content.substring(0, 300);
		}
		String snippet = checkTitle(content, title);
		if (snippet.trim().length() <= 300) {
			return snippet;
		}
		if (snippet.length() > 298) {
			snippet = snippet.substring(0, 298);
		}
		String [] str = snippet.split("(?<!\\s?(Гос|еп|Mr|Mrs|Дж|см|Ср|ср|Св|св|г|р|д|c|[А-ЯA-Z]|[0-9]))(\\.|\\!|\\?)((\\s?(?=[А-ЯA-Z]))|(\"))");
		String result = "";
		int i = 0;
		if (str.length > 1) {
			while ((result.length() + str[i].length()) < 298 && i < str.length-1) {
				result = result.concat(str[i] + ". ");
				i++;
			}
			return result.trim().concat("..");
		} else {
			return snippet.substring(0,snippet.lastIndexOf(" ")).concat("...");
		}
	}
	
	private static String checkTitle(String content, String title) {
		String text = content.toLowerCase().trim();
		String ttl = title.toLowerCase().trim();

		if (text.startsWith("-")) {
			return title.concat(" " + content);
		}

		if (text.indexOf(ttl)<=12 && text.indexOf(ttl)>=0) {
			return content;
		}
		if (content.trim().startsWith("и") || content.trim().startsWith("или")) {
			return title.concat(" " + content);
		}
		if (Character.isLowerCase(content.trim().charAt(0))) {
			return title.concat(" - " + content.trim());
		}
		if (Character.isUpperCase(content.trim().charAt(0))) {
			return title.concat(" : " + content.trim());
		}
		return title.concat(" " + content);
	}
}
