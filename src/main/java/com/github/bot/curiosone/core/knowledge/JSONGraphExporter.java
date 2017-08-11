package com.github.bot.curiosone.core.knowledge;

import java.util.Random;

import com.github.bot.curiosone.core.knowledge.Interfaces.Edge;
import com.github.bot.curiosone.core.knowledge.Interfaces.Graph;
import com.github.bot.curiosone.core.knowledge.Interfaces.GraphExporter;
import com.github.bot.curiosone.core.knowledge.Interfaces.Vertex;

/**
 * 
 * @author Christian
 *
 */
public class JSONGraphExporter implements GraphExporter{
	/**
	 *  tramite uno stringBuffer creo la stringa necessaria che servira poi 
	 *  per creare l'html del grafo scegliendo randomicamente il colore 
	 *  e facendo la stessa procedura per tutti i grafi e tutti i vertici
	 */
	@Override
	public String export(Graph g)
	{
		StringBuffer returner = new StringBuffer();
		returner.append("elements: [ "+"\n");
		String colore = "";
		for (Vertex node : g.vertexSet())
		{
			Random rand = new Random();
			int n= rand.nextInt(5);
			switch(n)
			{
			case 0: colore = "bg-red"; break;
			case 1: colore = "bg-green"; break;
			case 2: colore = "bg-purple"; break;
			case 3: colore = "bg-blue"; break;
			case 4: colore = "bg-salmon"; break;
			case 5: colore = "bg-grey";break;
			}
			returner.append("{"+"\n"+"	"+ "data : { id: '"+node.getId()+"' }, classes : '"+colore+"' \n}, \n");			
		}
		int count = 1;
		for (Edge arco : g.edgeSet())
		{
			returner.append("{"+"\n"+"	"+ "data : { source: '"+arco.getSource()+"', target: '"+
			arco.getTarget()+"', type: '"+arco.getType()+"'}, classes : '"+colore+"' \n}");
			if (count < g.edgeSet().size())
				returner.append(",");
			returner.append("\n");
			count +=1;
		}
		returner.append("]");
		return returner.toString();
	}
	
}
