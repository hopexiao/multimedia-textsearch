package edu.kit.iti.algo2.textindexing.searchengine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.kit.iti.algo2.textindexing.IIndex;
import edu.kit.iti.algo2.textindexing.searchengine.expr.Expr;
import edu.kit.iti.algo2.textindexing.searchengine.expr.MultiExpr;
import edu.kit.iti.algo2.textindexing.searchengine.expr.StrictWordExpr;

public class DefaultSearchEngine implements SearchEngine {
	private IIndex invertedIndex;
	private Map<StrictWordExpr, SearchResult> lookupCache = new HashMap<>();

	public DefaultSearchEngine(IIndex invertedIndex) {
		this.invertedIndex = invertedIndex;
	}

	@Override
	public SearchResult find(Expr expr) {
		reset();
		List<StrictWordExpr> swes = new LinkedList<>();
		gatherLookups(expr, swes);

		return visit(expr);
	}

	private SearchResult visit(Expr expr) {
		if (expr instanceof StrictWordExpr) {
			return lookupCache.get(expr);
		}

		if (expr instanceof MultiExpr) {
			MultiExpr me = (MultiExpr) expr;
			List<SearchResult> sr = new LinkedList<>();

			for (Expr subexpr : me.getSubExpr()) {
				sr.add(visit(subexpr));
			}

			switch (me.getOperation()) {
			case AND:
				return intersect(sr);
			case OR:
				return union(sr);

			}

		}
		// TODO other cases
		return new SearchResult();
	}

	private SearchResult union(List<SearchResult> sr) {
		Iterator<SearchResult> iter = sr.iterator();

		if (!iter.hasNext())
			return new SearchResult();

		SearchResult current = iter.next();
		while (iter.hasNext()) {
			current = current.union(iter.next());
		}
		return current;
	}

	private SearchResult intersect(List<SearchResult> sr) {
		Iterator<SearchResult> iter = sr.iterator();

		if (!iter.hasNext())
			return new SearchResult();

		SearchResult current = iter.next();
		while (iter.hasNext()) {
			current = current.intersect(iter.next());
		}
		return current;
	}

	private void reset() {
		lookupCache.clear();

	}

	private void gatherLookups(Expr expr, List<StrictWordExpr> swes) {
		if (expr instanceof StrictWordExpr) {
			StrictWordExpr strictWordExpr = (StrictWordExpr) expr;
			lookupCache.put(strictWordExpr,
					invertedIndex.lookup(strictWordExpr.getWord()));
			return;
		}

		if (expr instanceof MultiExpr) {
			MultiExpr me = (MultiExpr) expr;
			for (Expr e : me.child()) {
				gatherLookups(e, swes);
			}
		}
	}
}
