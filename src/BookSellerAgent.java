package jadelab2;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import javax.sound.sampled.Port;

import com.oracle.jrockit.jfr.Producer;

public class BookSellerAgent extends Agent {
  private Hashtable catalogue;
  private Hashtable pending;
  private BookSellerGui myGui;
  private boolean waitingForConfirmation = false;

  protected void setup() {
    catalogue = new Hashtable();
    pending = new Hashtable();
    myGui = new BookSellerGui(this);
    myGui.display();

    //book selling service registration at DF
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    ServiceDescription sd = new ServiceDescription();
    sd.setType("book-selling");
    sd.setName("JADE-book-trading");
    dfd.addServices(sd);
    try {
      DFService.register(this, dfd);
    }
    catch (FIPAException fe) {
      fe.printStackTrace();
    }
    
    addBehaviour(new OfferRequestsServer());

    addBehaviour(new PurchaseOrdersServer());

    addBehaviour(new PurchaseOrderRejection());
  }

  protected void takeDown() {
    //book selling service deregistration at DF
    try {
      DFService.deregister(this);
    }
    catch (FIPAException fe) {
      fe.printStackTrace();
    }
  	myGui.dispose();
    System.out.println("Seller agent " + getAID().getName() + " terminated.");
  }

  //invoked from GUI, when a new book is added to the catalogue
  public void updateCatalogue(final String title, final int price, final int shippingPrice) {
    addBehaviour(new OneShotBehaviour() {
      public void action() {
      	Product product = new Product(title, price, shippingPrice);
		catalogue.put(product.getName(), product);
		System.out.println(getAID().getLocalName() + ": " + title + " put into the catalogue. Price = " + price + "zl. Shipping price: " + shippingPrice + "zl.");
      }
    } );
  }
  
	private class OfferRequestsServer extends CyclicBehaviour {
	  public void action() {
	    //proposals only template
      MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
      ACLMessage msg = myAgent.receive(mt);
	    if (msg != null) {
	      String title = msg.getContent();
	      ACLMessage reply = msg.createReply();
	      Product product = (Product) catalogue.remove(title);
	      if (product != null) {
	        //title found in the catalogue, respond with its price as a proposal
	        reply.setPerformative(ACLMessage.PROPOSE);
	        pending.put(product.getName(), product);
          try {
            reply.setContentObject(product);
          } catch (IOException e) {
            e.printStackTrace();
          }
		    }
	      else {
	        //title not found in the catalogue
	        reply.setPerformative(ACLMessage.REFUSE);
	        reply.setContent("not-available");
	      }
	      myAgent.send(reply);
	    }
	    else {
	      block();
	    }
	  }
	}

	
	private class PurchaseOrdersServer extends CyclicBehaviour {
	  public void action() {
	    //purchase order as proposal acceptance only template
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
		ACLMessage msg = myAgent.receive(mt);
	    if (msg != null) {
	      String title = msg.getContent();
	      ACLMessage reply = msg.createReply();
	      Product product = (Product) pending.remove(title);
	      if (product != null) {
	        reply.setPerformative(ACLMessage.INFORM);
          System.out.println(getAID().getLocalName() + ": " + title + " sold to " + msg.getSender().getLocalName());
	      } else {
	        //title not found in the catalogue, sold to another agent in the meantime (after proposal submission)
	        reply.setPerformative(ACLMessage.FAILURE);
	        reply.setContent("not-available");
	      }
	      myAgent.send(reply);
	      waitingForConfirmation = false;
	    }
	    else {
		  block();
		}
	  }
	}

	private class PurchaseOrderRejection extends CyclicBehaviour {
    @Override
		public void action() {
  			// Puchase has been rejected
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
      
			if (msg != null) {
        Product product = (Product) pending.remove(msg.getContent());
        System.out.println(getAID().getLocalName() + ": Proposal has been rejected!");
        if (product != null) catalogue.put(product.getName(), product);
			} else {
				block();
			}
		}
	}

}
