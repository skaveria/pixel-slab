goog.provide('slab.tokens');
slab.tokens.token_interactive_re = (new RegExp("\\{\\{([A-Za-z0-9_]+)\\*\\}\\}","g"));
slab.tokens.current_page_stem = (function slab$tokens$current_page_stem(){
var p = (function (){var or__5002__auto__ = location.pathname;
if(cljs.core.truth_(or__5002__auto__)){
return or__5002__auto__;
} else {
return "index.html";
}
})();
var base = clojure.string.replace(clojure.string.replace(clojure.string.replace(clojure.string.replace(p,/.*\//,""),/\?.*$/,""),/#.*$/,""),/\.html?$/,"");
if(cljs.core.seq(base)){
return base;
} else {
return "index";
}
});
/**
 * Nearest Org export outline container id.
 *   Org HTML usually emits: <div id="outline-container-...">
 */
slab.tokens.find_section_id = (function slab$tokens$find_section_id(el){
var cur = el;
while(true){
if((cur == null)){
return null;
} else {
if((function (){var id = cur.id;
return ((typeof id === 'string') && (clojure.string.starts_with_QMARK_(id,"outline-container-")));
})()){
return slab.tokens.id;
} else {
var G__6088 = cur.parentElement;
cur = G__6088;
continue;

}
}
break;
}
});
/**
 * Extract token metadata from an interactive token element.
 */
slab.tokens.token_info = (function slab$tokens$token_info(el){
return new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"token","token",-1211463215),slab.util.dataset(el,"slabToken"),new cljs.core.Keyword(null,"token-id","token-id",-764089526),slab.util.dataset(el,"slabTokenId"),new cljs.core.Keyword(null,"section","section",-300141526),(function (){var or__5002__auto__ = slab.util.dataset(el,"slabSection");
if(cljs.core.truth_(or__5002__auto__)){
return or__5002__auto__;
} else {
var or__5002__auto____$1 = slab.tokens.find_section_id(el);
if(cljs.core.truth_(or__5002__auto____$1)){
return or__5002__auto____$1;
} else {
return slab.tokens.current_page_stem();
}
}
})(),new cljs.core.Keyword(null,"params","params",710516235),cljs.core.PersistentArrayMap.EMPTY], null);
});
slab.tokens.mk_token_el = (function slab$tokens$mk_token_el(token,token_id,section){
var el = document.createElement("span");
(el.className = "slab-token slab-token--interactive");

el.setAttribute("data-slab-action","run-token");

el.setAttribute("data-slab-token",token);

el.setAttribute("data-slab-token-id",token_id);

if(cljs.core.truth_(section)){
el.setAttribute("data-slab-section",section);
} else {
}

(el.textContent = [cljs.core.str.cljs$core$IFn$_invoke$arity$1(token),"*"].join(''));

return el;
});
slab.tokens.mk_slot_el = (function slab$tokens$mk_slot_el(token_id){
var slot = document.createElement("div");
(slot.className = "slab-output-slot");

slot.setAttribute("data-slab-slot",token_id);

return slot;
});
/**
 * Replace a text node with nodes.
 */
slab.tokens.replace_text_node_BANG_ = (function slab$tokens$replace_text_node_BANG_(text_node,nodes){
var temp__5804__auto__ = text_node.parentNode;
if(cljs.core.truth_(temp__5804__auto__)){
var p = temp__5804__auto__;
var seq__6050_6089 = cljs.core.seq(nodes);
var chunk__6051_6090 = null;
var count__6052_6091 = (0);
var i__6053_6092 = (0);
while(true){
if((i__6053_6092 < count__6052_6091)){
var n_6093 = chunk__6051_6090.cljs$core$IIndexed$_nth$arity$2(null,i__6053_6092);
p.insertBefore(n_6093,text_node);


var G__6094 = seq__6050_6089;
var G__6095 = chunk__6051_6090;
var G__6096 = count__6052_6091;
var G__6097 = (i__6053_6092 + (1));
seq__6050_6089 = G__6094;
chunk__6051_6090 = G__6095;
count__6052_6091 = G__6096;
i__6053_6092 = G__6097;
continue;
} else {
var temp__5804__auto___6098__$1 = cljs.core.seq(seq__6050_6089);
if(temp__5804__auto___6098__$1){
var seq__6050_6099__$1 = temp__5804__auto___6098__$1;
if(cljs.core.chunked_seq_QMARK_(seq__6050_6099__$1)){
var c__5525__auto___6100 = cljs.core.chunk_first(seq__6050_6099__$1);
var G__6101 = cljs.core.chunk_rest(seq__6050_6099__$1);
var G__6102 = c__5525__auto___6100;
var G__6103 = cljs.core.count(c__5525__auto___6100);
var G__6104 = (0);
seq__6050_6089 = G__6101;
chunk__6051_6090 = G__6102;
count__6052_6091 = G__6103;
i__6053_6092 = G__6104;
continue;
} else {
var n_6105 = cljs.core.first(seq__6050_6099__$1);
p.insertBefore(n_6105,text_node);


var G__6106 = cljs.core.next(seq__6050_6099__$1);
var G__6107 = null;
var G__6108 = (0);
var G__6109 = (0);
seq__6050_6089 = G__6106;
chunk__6051_6090 = G__6107;
count__6052_6091 = G__6108;
i__6053_6092 = G__6109;
continue;
}
} else {
}
}
break;
}

return p.removeChild(text_node);
} else {
return null;
}
});
slab.tokens.reset_regex_BANG_ = (function slab$tokens$reset_regex_BANG_(re){
(re.lastIndex = (0));

return re;
});
/**
 * Turn a text string containing {{name*}} into a vector of DOM nodes.
 *   Returns nil if no matches.
 */
slab.tokens.build_replacement_nodes = (function slab$tokens$build_replacement_nodes(txt,p__6058){
var map__6059 = p__6058;
var map__6059__$1 = cljs.core.__destructure_map(map__6059);
var page = cljs.core.get.cljs$core$IFn$_invoke$arity$2(map__6059__$1,new cljs.core.Keyword(null,"page","page",849072397));
var section = cljs.core.get.cljs$core$IFn$_invoke$arity$2(map__6059__$1,new cljs.core.Keyword(null,"section","section",-300141526));
var counter = cljs.core.get.cljs$core$IFn$_invoke$arity$2(map__6059__$1,new cljs.core.Keyword(null,"counter","counter",804008177));
slab.tokens.reset_regex_BANG_(slab.tokens.token_interactive_re);

var last_idx = (0);
var m = slab.tokens.token_interactive_re.exec(txt);
var out = cljs.core.PersistentVector.EMPTY;
while(true){
if((m == null)){
var tail = txt.substring(last_idx);
if(cljs.core.seq(out)){
var G__6073 = out;
if(cljs.core.seq(tail)){
return cljs.core.conj.cljs$core$IFn$_invoke$arity$2(G__6073,document.createTextNode(tail));
} else {
return G__6073;
}
} else {
return null;
}
} else {
var match_idx = m.index;
var token = (m[(1)]);
var match_str = (m[(0)]);
var before = txt.substring(last_idx,match_idx);
var i = cljs.core.swap_BANG_.cljs$core$IFn$_invoke$arity$2(counter,cljs.core.inc);
var token_id = [cljs.core.str.cljs$core$IFn$_invoke$arity$1(page),"::",cljs.core.str.cljs$core$IFn$_invoke$arity$1(token),"::",cljs.core.str.cljs$core$IFn$_invoke$arity$1((i - (1)))].join('');
var token_el = slab.tokens.mk_token_el(token,token_id,section);
var slot_el = slab.tokens.mk_slot_el(token_id);
var G__6110 = (match_idx + match_str.length);
var G__6111 = slab.tokens.token_interactive_re.exec(txt);
var G__6112 = (function (){var G__6077 = out;
var G__6077__$1 = ((cljs.core.seq(before))?cljs.core.conj.cljs$core$IFn$_invoke$arity$2(G__6077,document.createTextNode(before)):G__6077);
var G__6077__$2 = cljs.core.conj.cljs$core$IFn$_invoke$arity$2(G__6077__$1,token_el)
;
return cljs.core.conj.cljs$core$IFn$_invoke$arity$2(G__6077__$2,slot_el);

})();
last_idx = G__6110;
m = G__6111;
out = G__6112;
continue;
}
break;
}
});
/**
 * Call f on each text node under root.
 */
slab.tokens.walk_text_nodes = (function slab$tokens$walk_text_nodes(root,f){
var walker = document.createTreeWalker(root,NodeFilter.SHOW_TEXT,null,false);
var node = walker.nextNode();
while(true){
if(cljs.core.truth_(node)){
(f.cljs$core$IFn$_invoke$arity$1 ? f.cljs$core$IFn$_invoke$arity$1(node) : f.call(null,node));

var G__6113 = walker.nextNode();
node = G__6113;
continue;
} else {
return null;
}
break;
}
});
/**
 * Find {{name*}} occurrences in text nodes and replace with clickable tokens + slots.
 */
slab.tokens.hydrate_interactive_tokens_BANG_ = (function slab$tokens$hydrate_interactive_tokens_BANG_(){
var root = (function (){var or__5002__auto__ = document.getElementById("content");
if(cljs.core.truth_(or__5002__auto__)){
return or__5002__auto__;
} else {
return document.body;
}
})();
if(cljs.core.truth_(root)){
var page = slab.tokens.current_page_stem();
var counter = cljs.core.atom.cljs$core$IFn$_invoke$arity$1((0));
return slab.tokens.walk_text_nodes(root,(function (node){
var txt = node.nodeValue;
if(cljs.core.truth_((function (){var and__5000__auto__ = typeof txt === 'string';
if(and__5000__auto__){
var and__5000__auto____$1 = cljs.core.re_find(/\{\{/,txt);
if(cljs.core.truth_(and__5000__auto____$1)){
return cljs.core.re_find(slab.tokens.token_interactive_re,txt);
} else {
return and__5000__auto____$1;
}
} else {
return and__5000__auto__;
}
})())){
var section = slab.tokens.find_section_id(node.parentElement);
var nodes = slab.tokens.build_replacement_nodes(txt,new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"page","page",849072397),page,new cljs.core.Keyword(null,"section","section",-300141526),section,new cljs.core.Keyword(null,"counter","counter",804008177),counter], null));
if(cljs.core.seq(nodes)){
return slab.tokens.replace_text_node_BANG_(node,nodes);
} else {
return null;
}
} else {
return null;
}
}));
} else {
return null;
}
});

//# sourceMappingURL=slab.tokens.js.map
