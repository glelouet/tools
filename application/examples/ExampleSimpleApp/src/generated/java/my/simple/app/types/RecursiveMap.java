package my.simple.app.types;

import java.util.LinkedHashMap;

@SuppressWarnings("serial")
public class RecursiveMap
    extends LinkedHashMap<RecursiveMap, RecursiveMap>
{
}
