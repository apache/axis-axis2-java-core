package org.apache.axis.wsdl.tojava;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author chathura@opensource.lk
 *
 */
public class CommandLineOptionParser implements CommandLineOptionConstants{


	private Map commandLineOptions;
	
	public CommandLineOptionParser(String[] args){
		this.commandLineOptions = this.parse(args);
		
	}
	
	/**
	 * Return a list with <code>CommandLineOption</code> objects
	 * @param args
	 * @return CommandLineOption List
	 */
	private Map parse(String[] args){
		Map commandLineOptions = new HashMap();
		for(int i=0; i< args.length; i= i+2){
			CommandLineOption commandLineOption = new CommandLineOption(args[i], args[i+1]);
			commandLineOptions.put(commandLineOption.getType(), commandLineOption);
		}		
		return commandLineOptions;

	}
	
	
	public Map getAllOptions(){
		return this.commandLineOptions;
	}
	
	public List getInvalidOptions(){
		List faultList = new ArrayList();
		Iterator iterator = this.commandLineOptions.values().iterator();
		while (iterator.hasNext()){		
			CommandLineOption commandLineOption = ((CommandLineOption)(iterator.next()));
			if(commandLineOption.isInvalid()){
				faultList.add(commandLineOption);
			}
		}
		
		return faultList;
	}
	
	
}
