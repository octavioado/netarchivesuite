logfilePrefix = "scripting_events"   // A logfile will be created with this prefix + ".log"
//initials = "ABC"   // Curator initials to be added to log-messages

// To use, just remove the initial "//" from any one of these lines.
//
//killToeThread  1       //Kill a toe thread by number
//listFrontier '.*stats.*'    //List uris in the frontier matching a given regexp
//deleteFromFrontier '.*foobar.*'    //Remove uris matching a given regexp from the frontier
//printCrawlLog '.*'          //View already crawled lines uris matching a given regexp

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus

import java.nio.file.Files
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.logging.FileHandler
import java.util.logging.Logger;

void killToeThread(int thread) {
    job.crawlController.requestCrawlPause();
    job.crawlController.killThread(thread, true);
    logEvent("Killed Toe Thread number " + thread + ".")
    rawOut.println "WARNING: This job and heritrix may now need to be manually terminated when it is finished harvesting."
    rawOut.println "REMINDER: This job is now in a Paused state."
}

/**
 * Utility method to find and return the logger for a given log-prefix, or initialise it if it doesn't already exist.
 * @return logger
 */
Logger getLogger() {
    for (Map.Entry<Logger,FileHandler> entry: job.crawlController.loggerModule.fileHandlers ) {
        if (entry.key.name.contains(logfilePrefix)) {
            return entry.key
        }
    }
    return job.crawlController.loggerModule.setupSimpleLog(logfilePrefix);
}

void logEvent(String e) {
    getLogger().info("Action from user " + initials + ": " +e)
}

/* write some lines in a file, in a directory with an extension */
void writeToFile(def directory, def fileName, def extension, def infoList) {
    String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" 
    new File("$directory/$fileName$extension").withWriterAppend { out ->
        infoList.each {
            out.println new Date().format(dateFormat) + " " + it
        }
    }
}

/* to log some lines in a changelog.txt file (will be in the metadata.warc */
void logToScriptingEventsLogFile(def logLine) {
	def text = []
	text << logLine
	writeToFile(job.jobDir.absolutePath, "scripting_events", ".log", text)
}

void deleteFromFrontier(String regex) {
    job.crawlController.requestCrawlPause()
    count = job.crawlController.frontier.deleteURIs(".*", regex)
    rawOut.println "REMINDER: This job is now in a Paused state."
    logEvent("Deleted " + count + " uris matching regex '" + regex + "'")
    rawOut.println count + " uris deleted from frontier."
    rawOut.println("This action has been logged in " + logfilePrefix + ".log")
}

void listFrontier(String regex, long limit) {
    //style = 'overflow: auto; word-wrap: normal; white-space: pre; width:1200px; height:500px'
    //htmlOut.println '<pre style="' + style +'">'
    htmlOut.println '<pre>'
    pattern = ~regex
    //type  org.archive.crawler.frontier.BdbMultipleWorkQueues
    pendingUris = job.crawlController.frontier.pendingUris
    htmlOut.println 'queue items: ' + pendingUris.pendingUrisDB.count()
    //iterates over the raw underlying instance of com.sleepycat.je.Database
    cursor = pendingUris.pendingUrisDB.openCursor(null, null);
    key = new DatabaseEntry();
    value = new DatabaseEntry();
    matchingCount = 0
    try {
        while (cursor.getNext(key, value, null) == OperationStatus.SUCCESS && limit > 0) {
            if (value.getData().length == 0) {
                continue;
            }
            curi = pendingUris.crawlUriBinding.entryToObject(value);
            if (pattern.matcher(curi.toString())) {
                //htmlOut.println '<span style="font-size:small;">' + curi + '</span>'
                htmlOut.println curi
                matchingCount++
                --limit;
            }
        }
    } finally {
        cursor.close();
    }
    htmlOut.println '</pre>'
    if (limit > 0) {
        htmlOut.println '<p>'+ matchingCount + " matching uris found </p>"
    } else {
        htmlOut.println '<p>The first ' + matchingCount + " matching uris found. (Return limit reached)</p>"
    }
}

void pageFrontier(long skip, int items) {
    htmlOut.println '<pre>'
    //pattern = ~regex
    //type  org.archive.crawler.frontier.BdbMultipleWorkQueues
    pendingUris = job.crawlController.frontier.pendingUris
    //iterates over the raw underlying instance of com.sleepycat.je.Database
    cursor = pendingUris.pendingUrisDB.openCursor(null, null);
    key = new DatabaseEntry();
    value = new DatabaseEntry();
    cursor.skipNext(skip, key, value, null)
    matchingCount = 0
    try {
        while (cursor.getNext(key, value, null) == OperationStatus.SUCCESS) {
            if (value.getData().length == 0) {
                continue;
            }
            curi = pendingUris.crawlUriBinding.entryToObject(value);
            htmlOut.println '<span style="font-size:small;">' + curi + '</span>'
            /*
            if (pattern.matcher(curi.toString())) {
                htmlOut.println '<span style="font-size:small;">' + curi + '</span>'
                matchingCount++
            }
            */
        }
    } finally {
        cursor.close();
    }
    htmlOut.println '</pre>'
    htmlOut.println '<p>'+ matchingCount + " matching uris found </p>"
}

class patternMatchingPredicate implements Predicate<String> {
    private java.util.regex.Pattern p;
    public patternMatchingPredicate(java.util.regex.Pattern p) {this.p=p;}
    boolean test(String s) {return s.matches(p);}
}

class PrintConsumer implements Consumer<String> {
    private PrintWriter out;
    public PrintConsumer(PrintWriter out){this.out=out;}
    void accept(String s) {out.println("<span style=\"font-size:small\">" + s + "</span>");}
}

void printCrawlLog(String regex) {
    style = 'overflow: auto; word-wrap: normal; white-space: pre; width:1200px; height:500px'
    htmlOut.println '<pre style="' + style +'">'
    namePredicate = new patternMatchingPredicate(~regex);
    crawlLogFile = job.crawlController.frontier.loggerModule.crawlLogPath.file
    matchingCount =  Files.lines(crawlLogFile.toPath()).filter(namePredicate).peek(new PrintConsumer(htmlOut)).count()
    htmlOut.println '</pre>'
    htmlOut.println '<p>'+ matchingCount + " matching lines found </p>"
}

void showModBudgets() {
	def modQueues = job.jobContext.data.get("manually-added-queues");
	if(modQueues.size() > 0) {
		htmlOut.println('<p>Budgets of following domains/hosts have been changed in the current job :</p>')
	}
	htmlOut.println('<ul>')
	modQueues.each { key, value ->
		htmlOut.println('<li>'+key)
		htmlOut.println('<input type="text" name="'+key+'-budget" value="'+value+'"/>')
		htmlOut.println('<button type="submit" name="submitButton" value="'+key+'" class="btn btn-success"><i class="icon-white icon-thumbs-up"></i> Save</button></li>')
	}
	htmlOut.println('</ul>')
}

void changeBudget(String key, int value) {
	queue = appCtx.getBean("frontier").getQueueFor(key)
	queue.totalBudget = value

	//to store our manually added budget changes, we have to put them in a map
	def modQueues = job.jobContext.data.get("manually-added-queues");
	if(modQueues == null) {
		modQueues = [:]
	}
	modQueues.put(key, queue.totalBudget)
	job.jobContext.data.put("manually-added-queues", modQueues)
	
	logToScriptingEventsLogFile("manual budget change : "+ key + " -> "+value)
}

void getQueueTotalBudget() {
	htmlOut.println appCtx.getBean("frontier").queueTotalBudget
}


void showFilters() {
	def filters = job.jobContext.data.get("manually-added-rejected-filters")
	htmlOut.println('<ul>')
	filters.eachWithIndex{ val, idx -> 
		htmlOut.println('<li><input type="checkbox" name="removeIndex" value="'+idx+'" />&nbsp;'+val+'</li>')
	}
	htmlOut.println('</ul>')
	if(filters.size() > 0) {
		htmlOut.println('<button type="submit" name="remove-filter" value="1" class="btn btn-success"><i class="icon-white icon-remove"></i> Remove</button>')
	}
}

void addFilter(String pat) {
	if(pat.length() > 0) {
		regexRuleObj = appCtx.getBean("scope").rules.find{ it.class == org.archive.modules.deciderules.MatchesListRegexDecideRule }
		regexRuleObj.regexList.add(pat)
		//to store our manually added filters, we have to put them in a map
		def filters = job.jobContext.data.get("manually-added-rejected-filters");
		if(filters == null) {
			filters = [] as ArrayList
		}
		filters << pat
		job.jobContext.data.put("manually-added-rejected-filters", filters)
	
		logToScriptingEventsLogFile("manual add of a DecideResult.REJECT filter : "+ pat)
	}
}

void removeFilters(def indexesOFiltersToRemove) {
	regexRuleObj = appCtx.getBean("scope").rules.find{ it.class == org.archive.modules.deciderules.MatchesListRegexDecideRule }
	def filters = job.jobContext.data.get("manually-added-rejected-filters")
	indexesOFiltersToRemove.each {
		//remove from the manually added filters map
		regex = filters.get(it)
		index = regexRuleObj.regexList.indexOf(regex)
		if(index != -1) {
			regexRuleObj.regexList.remove(index)
			filters.remove(it)
			logToScriptingEventsLogFile("removing DecideResult.REJECT filter : "+ regex)
		}
		
	}
}
