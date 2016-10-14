/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package localdata;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class LocalDataCollectorCrawler extends WebCrawler {
	private static final Logger logger = LoggerFactory
			.getLogger(LocalDataCollectorCrawler.class);
	
	private static int id;

	private static final Pattern FILTERS = Pattern
			.compile(".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf"
					+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

	CrawlStat myCrawlStat;

	public LocalDataCollectorCrawler() {
		//id = 95000;
		myCrawlStat = new CrawlStat();
	}

	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		return !FILTERS.matcher(href).matches()
				&& href.startsWith("https://angel.co/");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(Page page) {
		logger.info("Visited: {}", page.getWebURL().getURL());
		myCrawlStat.incProcessedPages();
		
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData parseData = (HtmlParseData) page.getParseData();
			Set<WebURL> links = parseData.getOutgoingUrls();
			myCrawlStat.incTotalLinks(links.size());
			try {
				String data = parseData.getText();
				data = data.replaceAll("\\n", " ").replaceAll("\\t", " ");
				myCrawlStat.incTotalTextSize(data.getBytes("UTF-8").length);
				id++;
				String fileName = "doc" + id + ".json";
				FileWriter file = new FileWriter("C:\\Users\\Chandani\\Desktop\\CrawledData\\" + fileName);
				JSONObject obj = new JSONObject();
				obj.put("id", id);
				obj.put("name", fileName);
				obj.put("url", page.getWebURL().getURL());
				obj.put("title", parseData.getTitle());
				obj.put("outgoingLinks", links);
				obj.put("data", data);
				file.write(obj.toJSONString().trim().replaceAll("\\\\/", "/"));
				file.close();
			} catch (UnsupportedEncodingException ignored) {
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// We dump this crawler statistics after processing every 50 pages
		if ((myCrawlStat.getTotalProcessedPages() % 50) == 0) {
			dumpMyData();
		}
	}

	/**
	 * This function is called by controller to get the local data of this
	 * crawler when job is finished
	 */
	@Override
	public Object getMyLocalData() {
		return myCrawlStat;
	}

	/**
	 * This function is called by controller before finishing the job. You can
	 * put whatever stuff you need here.
	 */
	@Override
	public void onBeforeExit() {
		dumpMyData();
	}

	public void dumpMyData() {
		int id = getMyId();
		// You can configure the log to output to file
		logger.info("Crawler {} > Processed Pages: {}", id,
				myCrawlStat.getTotalProcessedPages());
		logger.info("Crawler {} > Total Links Found: {}", id,
				myCrawlStat.getTotalLinks());
		logger.info("Crawler {} > Total Text Size: {}", id,
				myCrawlStat.getTotalTextSize());
	}
}