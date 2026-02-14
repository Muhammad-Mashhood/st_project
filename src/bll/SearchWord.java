package bll;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dto.Documents;
import dto.Pages;
import pl.EditorPO;

public class SearchWord {
	public static List<String> searchKeyword(String keyword, List<Documents> docs) {
		final Logger LOGGER = LogManager.getLogger(EditorPO.class);
		// TODO Auto-generated method stub
		List<String> getFiles = new ArrayList<>();
		if (keyword.length() < 3) {
			throw new IllegalArgumentException("Could not Search, Please Enter at least 3 letter to search");
		}

		for (Documents doc : docs) {
			for (Pages page : doc.getPages()) {
				String pageContent = page.getPageContent();
				if (pageContent.toLowerCase().contains(keyword.toLowerCase())) {

					String[] words = pageContent.split("\\s+");

					for (int i = 0; i < words.length; i++) {
						if (words[i].equalsIgnoreCase(keyword)) {

							String prefixWord;
							if (i > 0) {
								prefixWord = words[i - 1];
							} else {
								prefixWord = "";
							}
							getFiles.add(doc.getName() + " - " + prefixWord + " " + keyword + "...");
							break;
						}
					}
					break;
				}
			}
		}
		return getFiles;
	}

}
