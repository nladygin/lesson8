import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;


class SiteTree extends HashMap<String, List<String>> {

    private int treeSize = 0;
    private int branchCount = 0;
    private String rootURL;

    public SiteTree(String startPage) {
        this.rootURL = startPage;
        this.put(startPage, null);
    }


    public void addLinks(String page, List<String> hrefs) {
        for (String href : hrefs) {
            if ( !this.containsKey(href)
                    && href.contains(rootURL)
                    && !href.contains("#")
            ) {
                this.put(href, null);
            }
        }
        this.put(page, hrefs);
        this.branchCount += hrefs.size();
    }


    public Entry getNextSourcePage() {
        Iterator hmIterator = this.entrySet().iterator();
        do {
            SiteTree.Entry mapElement = (Map.Entry) hmIterator.next();
            if (mapElement.getValue() == null) {
                return mapElement;
            }
        } while (hmIterator.hasNext());
        return null;
    }


    public void showTreeSize(Logger logger) {
        int ts = this.size()/100;
        if (ts > this.treeSize) {
            this.treeSize = ts;
            logger.info("tree size: " + ts + "00+");
        }
    }


    public int getBranchCount() {
        return this.branchCount;
    }

}
