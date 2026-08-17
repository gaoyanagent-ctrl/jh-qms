package com.company.iaf.mdm.domain.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Allow-listed JSON Logic subset; never executes scripts or reflected methods. */
public final class JsonLogicEvaluator {
    public boolean matches(Map<String, Object> expression, Map<String, Object> data) {
        return expression == null || expression.isEmpty() || truthy(evaluate(expression, data));
    }
    private Object evaluate(Object node, Map<String, Object> data) {
        if (!(node instanceof Map<?, ?> map) || map.isEmpty()) return node;
        var entry = map.entrySet().iterator().next(); String operator = String.valueOf(entry.getKey()); Object argument = entry.getValue();
        if ("var".equals(operator)) return resolve(data, String.valueOf(argument));
        List<?> args = argument instanceof List<?> list ? list : List.of(argument);
        return switch (operator) {
            case "==" -> value(args,0,data).equals(value(args,1,data));
            case "!=" -> !value(args,0,data).equals(value(args,1,data));
            case "and" -> args.stream().allMatch(item -> truthy(evaluate(item,data)));
            case "or" -> args.stream().anyMatch(item -> truthy(evaluate(item,data)));
            case "!" -> !truthy(value(args,0,data));
            case ">", ">=", "<", "<=" -> compare(value(args,0,data),value(args,1,data),operator);
            case "in" -> contains(value(args,1,data),value(args,0,data));
            default -> false;
        };
    }
    private Object value(List<?> args,int index,Map<String,Object> data){return index>=args.size()?"":java.util.Objects.requireNonNullElse(evaluate(args.get(index),data),"");}
    private Object resolve(Map<String,Object> data,String path){Object value=data;for(String part:path.split("\\.")){if(!(value instanceof Map<?,?> map))return null;value=map.get(part);}return value;}
    private boolean compare(Object left,Object right,String operator){try{int result=new BigDecimal(String.valueOf(left)).compareTo(new BigDecimal(String.valueOf(right)));return switch(operator){case ">"->result>0;case ">="->result>=0;case "<"->result<0;default->result<=0;};}catch(NumberFormatException ignored){return false;}}
    private boolean contains(Object container,Object item){return container instanceof List<?> list?list.contains(item):String.valueOf(container).contains(String.valueOf(item));}
    private boolean truthy(Object value){return value instanceof Boolean b?b:value!=null&&!"".equals(value)&&!Boolean.FALSE.equals(value);}
}
