package hiks.yourlists.server.model;

import java.util.Iterator;
import java.util.List;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class ItemList {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;

    @Persistent
    private String name;
    
    @Element(dependent = "true")
    private List<Item> items;
    
    public ItemList() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	/**
	 * @return the items
	 */
	public List<Item> getItems() {
		return items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(List<Item> items) {
		this.items = items;
	}
	/**
	 * Add a new item to the list
	 * @param item the item to add
	 */
	public void addItem(Item newItem){
		items.add(newItem);
	}
	
	public Item getItem(Long id){
		Iterator<Item> it = items.iterator();
		Item item;
		while(it.hasNext()){
			item = it.next();
			if (item.getKey().getId() == id){
				return item; 
			}
		}
		return null;
	}
}