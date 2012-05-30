package hiks.yourlists.client.model;

import java.util.List;

public class ItemList {

	    private Long id;

	    private String name;
	    
	    private List<Long> itemIds;
	    
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
		 * @return the itemIds
		 */
		public List<Long> getItemIds() {
			return itemIds;
		}
	
		/**
		 * @param itemIds the itemIds to set
		 */
		public void setItemIds(List<Long> itemIds) {
			this.itemIds = itemIds;
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
}
