package app;
import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.staticFiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import model.Status;
import model.Todo;
import model.TodoDao;
import spark.ModelAndView;
import spark.Request;
import spark.template.velocity.VelocityTemplateEngine;
import util.Filters;
import util.MessageBundle;

/**
 * This class contains exactly the same functionality as TodoList, but it's
 * following normal Spark conventions more closely.
 */
@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
public class BasicTodoList {

	public static void main(String[] args) {

		

		exception(Exception.class, (e, req, res) -> e.printStackTrace()); // print all exceptions
		staticFiles.location("/public");
		port(7777);

		// Render main UI
		get("/", (req, res) -> renderTodos(req));

		// Add new
		post("/todos", (req, res) -> {
			TodoDao.add(Todo.create(req.queryParams("todo-title")));
			return renderTodos(req);
		});

		// Remove all completed
		delete("/todos/completed", (req, res) -> {
			TodoDao.removeCompleted();
			return renderTodos(req);
		});

		// Toggle all status
		put("/todos/toggle_status", (req, res) -> {
			TodoDao.toggleAll(req.queryParams("toggle-all") != null);
			return renderTodos(req);
		});

		// Remove by id
		delete("/todos/:id", (req, res) -> {
			TodoDao.remove(req.params("id"));
			return renderTodos(req);
		});

		// Update by id
		put("/todos/:id", (req, res) -> {
			TodoDao.update(req.params("id"), req.queryParams("todo-title"));
			return renderTodos(req);
		});

		// Toggle status by id
		put("/todos/:id/toggle_status", (req, res) -> {
			TodoDao.toggleStatus(req.params("id"));
			return renderTodos(req);
		});

		// Edit by id
		get("/todos/:id/edit", (req, res) -> renderEditTodo(req));
		
		before("*", Filters.handleLocaleChange);

	}

	private static String renderEditTodo(Request req) {
		return renderTemplate("velocity/editTodo.vm", new HashMap() {
			{
				put("todo", TodoDao.find(req.params("id")));
			}
		});
	}

	private static String renderTodos(Request req) {
		String statusStr = req.queryParams("status");
		Map<String, Object> model = new HashMap<>();
		model.put("msg", new MessageBundle(getSessionLocale(req)));
		model.put("todos", TodoDao.ofStatus(statusStr));
		model.put("filter", Optional.ofNullable(statusStr).orElse(""));
		model.put("activeCount", TodoDao.ofStatus(Status.ACTIVE).size());
		model.put("anyCompleteTodos", TodoDao.ofStatus(Status.COMPLETE).size() > 0);
		model.put("allComplete", TodoDao.all().size() == TodoDao.ofStatus(Status.COMPLETE).size());
		model.put("status", Optional.ofNullable(statusStr).orElse(""));
		if ("true".equals(req.queryParams("ic-request"))) {
			return renderTemplate("velocity/todoList.vm", model);
		}
		return renderTemplate("velocity/index.vm", model);
	}

	private static String renderTemplate(String template, Map model) {
		return new VelocityTemplateEngine().render(new ModelAndView(model, template));
	}
	
	private static String getSessionLocale(Request request) {
        return request.session().attribute("locale");
    }

}
