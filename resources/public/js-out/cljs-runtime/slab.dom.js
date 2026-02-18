goog.provide('slab.dom');
slab.dom.q1 = (function slab$dom$q1(sel){
return document.querySelector(sel);
});
/**
 * Find the output slot for a token-id. Returns the slot element or nil.
 *   Expected markup: <div data-slab-slot="TOKEN-ID"></div>
 */
slab.dom.ensure_output_slot_BANG_ = (function slab$dom$ensure_output_slot_BANG_(p__20827){
var map__20828 = p__20827;
var map__20828__$1 = cljs.core.__destructure_map(map__20828);
var token_id = cljs.core.get.cljs$core$IFn$_invoke$arity$2(map__20828__$1,new cljs.core.Keyword(null,"token-id","token-id",-764089526));
if(cljs.core.truth_(token_id)){
return slab.dom.q1(["[data-slab-slot=\"",cljs.core.str.cljs$core$IFn$_invoke$arity$1(token_id),"\"]"].join(''));
} else {
return null;
}
});
slab.dom.clear_BANG_ = (function slab$dom$clear_BANG_(el){
if(cljs.core.truth_(el)){
(el.innerHTML = "");

return el;
} else {
return null;
}
});
/**
 * Create terminal-style output block node.
 *   NOTE: map these class names to your existing CSS example container styles.
 */
slab.dom.terminal_block_node = (function slab$dom$terminal_block_node(p__20829){
var map__20832 = p__20829;
var map__20832__$1 = cljs.core.__destructure_map(map__20832);
var text = cljs.core.get.cljs$core$IFn$_invoke$arity$2(map__20832__$1,new cljs.core.Keyword(null,"text","text",-1790561697));
var loading_QMARK_ = cljs.core.get.cljs$core$IFn$_invoke$arity$2(map__20832__$1,new cljs.core.Keyword(null,"loading?","loading?",1905707049));
var pre = document.createElement("pre");
(pre.className = "slab-terminal");

(pre.textContent = (cljs.core.truth_(loading_QMARK_)?"\u258C":(function (){var or__5002__auto__ = text;
if(cljs.core.truth_(or__5002__auto__)){
return or__5002__auto__;
} else {
return "";
}
})()
));

return pre;
});
/**
 * Create media-scaled container node and append rich content.
 */
slab.dom.media_block_node = (function slab$dom$media_block_node(rich_node){
var wrap = document.createElement("div");
(wrap.className = "slab-media");

wrap.appendChild(rich_node);

return wrap;
});
slab.dom.show_loading_BANG_ = (function slab$dom$show_loading_BANG_(slot){
if(cljs.core.truth_(slot)){
slab.dom.clear_BANG_(slot);

slot.appendChild(slab.dom.terminal_block_node(new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"loading?","loading?",1905707049),true], null)));

return slot;
} else {
return null;
}
});
slab.dom.show_text_BANG_ = (function slab$dom$show_text_BANG_(slot,p__20838){
var map__20839 = p__20838;
var map__20839__$1 = cljs.core.__destructure_map(map__20839);
var body = cljs.core.get.cljs$core$IFn$_invoke$arity$2(map__20839__$1,new cljs.core.Keyword(null,"body","body",-2049205669));
if(cljs.core.truth_(slot)){
slab.dom.clear_BANG_(slot);

slot.appendChild(slab.dom.terminal_block_node(new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"text","text",-1790561697),body], null)));

return slot;
} else {
return null;
}
});
slab.dom.show_rich_BANG_ = (function slab$dom$show_rich_BANG_(slot,p__20841){
var map__20842 = p__20841;
var map__20842__$1 = cljs.core.__destructure_map(map__20842);
var body = cljs.core.get.cljs$core$IFn$_invoke$arity$2(map__20842__$1,new cljs.core.Keyword(null,"body","body",-2049205669));
if(cljs.core.truth_(slot)){
slab.dom.clear_BANG_(slot);

var node_20858 = slab.render.render_rich.cljs$core$IFn$_invoke$arity$1(body);
slot.appendChild(slab.dom.media_block_node(node_20858));

return slot;
} else {
return null;
}
});
/**
 * Raw, honest error output.
 */
slab.dom.show_error_BANG_ = (function slab$dom$show_error_BANG_(slot,err){
if(cljs.core.truth_(slot)){
slab.dom.clear_BANG_(slot);

slot.appendChild(slab.dom.terminal_block_node(new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"text","text",-1790561697),cljs.core.str.cljs$core$IFn$_invoke$arity$1(err)], null)));

return slot;
} else {
return null;
}
});

//# sourceMappingURL=slab.dom.js.map
