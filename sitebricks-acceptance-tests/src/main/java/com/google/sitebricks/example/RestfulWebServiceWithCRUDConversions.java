package com.google.sitebricks.example;

import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Patch;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@At(RestfulWebServiceWithCRUDConversions.AT_ME)
@Service
public class RestfulWebServiceWithCRUDConversions {
	public static final String AT_ME = "/jsonConversion";
	public static List<Widget> widgets = new ArrayList<Widget>();

	static {
		populate();
	}

	private static void populate() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
		try {
			addWidget(new Widget(1, "Widget 1", sdf.parse("01-JAN-1990 12:00"), 1.50));
			addWidget(new Widget(2, "Widget 2", sdf.parse("02-FEB-2000 18:00"), 21.75));
			addWidget(new Widget(3, "Widget 3", sdf.parse("03-MAR-2010 23:00"), 19.99));
		} catch (Exception e) {
		}
	}

	@Post
	public Reply<Widget> post(Request request) {
		Widget widget = request.read(Widget.class).as(Json.class);
		addWidget(widget);
		return Reply.with(widget).as(Json.class).type("application/json");
	}

	@Put
	public Reply<?> put(Request request) {
		Widget widget = request.read(Widget.class).as(Json.class);
		addWidget(widget);
		return Reply.with(widget).as(Json.class).type("application/json");
	}

  @Patch
  public Reply<?> patch(Request request) {
    Widget widget = request.read(Widget.class).as(Json.class);
    updateWidget(widget);
    return Reply.with(widget).as(Json.class).type("application/json");
  }

	@Get
	public Reply<List<Widget>> getAll() {
		return Reply.with(widgets).as(Json.class).type("application/json");
	}

	@At("/:id")
	@Get
	public Reply<?> get(@Named("id") int id) {
		Widget widget = findWidget(id);
		if (widget != null)
			return Reply.with(widget).as(Json.class).type("application/json");
		return Reply.saying().error();
	}

	@At("/:id")
	@Delete
	public Reply<?> delete(@Named("id") int id) {
		Widget widget = removeWidget(id);
		if (widget != null)
			return Reply.with(widget).as(Json.class).type("application/json");
		return Reply.saying().error();
	}

	public static Widget removeWidget(Widget widget) {
		return removeWidget(widget.getId());
	}

	public static Widget removeWidget(int id) {
		Widget oldWidget = findWidget(id);
		if (oldWidget != null)
			widgets.remove(oldWidget);
		return oldWidget;
	}

	public static void addWidget(Widget widget) {
		Widget oldWidget = findWidget(widget.getId());
		if (oldWidget != null)
			widgets.remove(oldWidget);
		widgets.add(widget);
	}

  public static void updateWidget(Widget widget) {
    Widget staleWidget = findWidget(widget.getId());
    if (staleWidget != null) {
      staleWidget.setName(widget.getName());
      staleWidget.setAvailable(widget.getAvailable());
      staleWidget.setPrice(widget.getPrice());
    }
  }

	public static Widget findWidget(int id) {
		for (Widget widget : widgets) {
			if (widget.getId() == id)
				return widget;
		}
		return null;
	}

	public static class Widget implements Serializable {
		int id;
		String name;
		Date available;
		double price;

		public Widget() {
		}

		public Widget(int id, String name, Date available, double price) {
			this.id = id;
			this.name = name;
			this.available = available;
			this.price = price;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Date getAvailable() {
			return available;
		}

		public void setAvailable(Date available) {
			this.available = available;
		}

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		public Widget clone()	{
			return new Widget (id, name, available, price);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((available == null) ? 0 : available.hashCode());
			result = prime * result + id;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			long temp;
			temp = Double.doubleToLongBits(price);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Widget other = (Widget) obj;
			if (available == null) {
				if (other.available != null)
					return false;
			} else if (!available.equals(other.available))
				return false;
			if (id != other.id)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (Double.doubleToLongBits(price) != Double.doubleToLongBits(other.price))
				return false;
			return true;
		}


	}
}
