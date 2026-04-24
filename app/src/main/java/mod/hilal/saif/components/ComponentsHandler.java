package mod.hilal.saif.components;

import static mod.hilal.saif.events.EventsHandler.capitalize;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.besome.sketch.beans.ComponentBean;
import com.google.gson.Gson;

import java.util.*;

import a.a.a.Lx;
import mod.hey.studios.util.Helper;
import mod.jbk.util.OldResourceIdMapper;
import pro.sketchware.R;
import pro.sketchware.SketchApplication;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.SketchwareUtil;

public class ComponentsHandler {

    private static ArrayList<HashMap<String, Object>> cachedCustomComponents = new ArrayList<>();

    // ⚡ FAST ACCESS MAPS
    private static final HashMap<Integer, HashMap<String, Object>> fastMap = new HashMap<>();
    private static final HashMap<String, HashMap<String, Object>> typeNameMap = new HashMap<>();

    private static boolean ready = false;

    // 🔥 DEBUG SWITCH
    private static final boolean DEBUG = true;

    private ComponentsHandler() {}

    // =========================
    // 🔧 INIT
    // =========================
    private static void ensureInit() {
        if (!ready) refreshCachedCustomComponents();
    }

    private static void logError(String msg) {
        if (DEBUG) {
            SketchwareUtil.toastError(msg, Toast.LENGTH_SHORT);
        }
    }

    // =========================
    // ⚡ BUILD FAST MAP
    // =========================
    private static void buildMaps() {

        fastMap.clear();
        typeNameMap.clear();

        HashSet<Integer> usedIds = new HashSet<>();
        HashSet<String> usedNames = new HashSet<>();

        for (int i = 0; i < cachedCustomComponents.size(); i++) {

            HashMap<String, Object> comp = cachedCustomComponents.get(i);

            if (comp == null) {
                logError("Null component at index " + i);
                continue;
            }

            if (!isValidComponent(comp)) {
                logError("Invalid structure at index " + i);
                continue;
            }

            try {

                String idStr = (String) comp.get("id");
                String type = (String) comp.get("typeName");

                if (TextUtils.isEmpty(idStr) || TextUtils.isEmpty(type)) {
                    logError("Missing id/type at index " + i);
                    continue;
                }

                int id = Integer.parseInt(idStr);

                if (usedIds.contains(id)) {
                    logError("Duplicate ID " + id + " at index " + i);
                    continue;
                }

                if (usedNames.contains(type)) {
                    logError("Duplicate typeName '" + type + "' at index " + i);
                    continue;
                }

                usedIds.add(id);
                usedNames.add(type);

                fastMap.put(id, comp);
                typeNameMap.put(type, comp);

            } catch (Exception e) {
                logError("Parse error at index " + i);
            }
        }
    }

    // =========================
    // SAFE GETTERS
    // =========================
    private static String getS(HashMap<String, Object> m, String k, String def) {
        if (m == null) return def;
        Object v = m.get(k);
        return (v instanceof String) ? (String) v : def;
    }

    private static HashMap<String, Object> byId(int id) {
        ensureInit();
        return fastMap.get(id);
    }

    private static HashMap<String, Object> byName(String name) {
        ensureInit();
        return typeNameMap.get(name);
    }

    // =========================
    // PUBLIC METHODS (UNCHANGED)
    // =========================

    public static int id(String name) {
        if ("AsyncTask".equals(name)) return 36;

        HashMap<String, Object> c = byName(name);

        if (c == null) {
            logError("Component not found: " + name);
            return -1;
        }

        try {
            return Integer.parseInt(getS(c, "id", "-1"));
        } catch (Exception e) {
            logError("Invalid ID for " + name);
            return -1;
        }
    }

    public static String typeName(int id) {
        if (id == 36) return "AsyncTask";

        HashMap<String, Object> c = byId(id);

        if (c == null) {
            logError("typeName not found for id " + id);
        }

        return getS(c, "typeName", "");
    }

    public static String name(int id) {
        if (id == 36) return "AsyncTask";

        HashMap<String, Object> c = byId(id);

        if (c == null) {
            logError("name not found for id " + id);
        }

        return getS(c, "name", "component");
    }

    public static int icon(int id) {
        if (id == 36) return R.drawable.ic_cycle_color_48dp;

        try {
            return OldResourceIdMapper.getDrawableFromOldResourceId(
                    Integer.parseInt(getS(byId(id), "icon", "0"))
            );
        } catch (Exception e) {
            logError("Invalid icon for id " + id);
            return R.drawable.color_new_96;
        }
    }

    public static String description(int id) {
        int res = ComponentBean.getDescStrResource(id);
        return res != 0
                ? SketchApplication.getContext().getString(res)
                : description2(id);
    }

    public static String description2(int id) {
        return getS(byId(id), "description", "new component");
    }

    public static String docs(int id) {
        if (id == 36) return "";
        return getS(byId(id), "url", "");
    }

    public static String getBuildClassById(int id) {
        if (id == 36) return "AsyncTask";
        return getS(byId(id), "buildClass", "");
    }

    public static void add(ArrayList<ComponentBean> list) {
        ensureInit();

        list.add(new ComponentBean(36));

        for (HashMap<String, Object> c : cachedCustomComponents) {
            try {
                list.add(new ComponentBean(Integer.parseInt(getS(c, "id", "-1"))));
            } catch (Exception e) {
                logError("Invalid component in add()");
            }
        }
    }

    public static String getTypeName(int id) {
        if (id == 36) return "#";
        return getS(byId(id), "typeName", "");
    }

    public static String getVarName(String name) {
        return getS(byName(name), "varName", name);
    }

    @NonNull
    public static String getClassByTypeName(@NonNull String name) {
        if (name.equals("AsyncTask")) return "Component.AsyncTask";
        return getS(byName(name), "class", "Component");
    }

    public static String extraVar(String name, String code, String varName) {

        String add = getS(byName(name), "additionalVar", "");

        if (TextUtils.isEmpty(add)) return code;

        return code + "\n" +
                add.replace("###", varName)
                        .replace("$name", varName)
                        .replace("$Name", capitalize(varName))
                        .replace("$NAME", varName.toUpperCase());
    }

    public static String defineExtraVar(String name, String varName) {

        String def = getS(byName(name), "defineAdditionalVar", "");

        if (TextUtils.isEmpty(def)) return "";

        return def.replace("###", varName)
                .replace("$name", varName)
                .replace("$Name", capitalize(varName))
                .replace("$NAME", varName.toUpperCase());
    }

    public static void getImports(String name, ArrayList<String> list) {

        String imp = getS(byName(name), "imports", "");

        if (!TextUtils.isEmpty(imp)) {
            list.addAll(Arrays.asList(imp.split("\n")));
        } else {
            logError("No imports for " + name);
        }
    }

    // =========================
    // FILE
    // =========================

    public static String getPath() {
        return FileUtil.getExternalStorageDir() + "/.sketchware/data/system/component.json";
    }

    private static ArrayList<HashMap<String, Object>> readCustomComponents() {

        try {

            if (!FileUtil.isExistFile(getPath())) {
                logError("Component JSON not found");
                return new ArrayList<>();
            }

            ArrayList<HashMap<String, Object>> data =
                    new Gson().fromJson(FileUtil.readFile(getPath()), Helper.TYPE_MAP_LIST);

            if (data == null) {
                logError("Invalid JSON structure");
                return new ArrayList<>();
            }

            return data;

        } catch (Exception e) {
            logError("JSON read error");
            return new ArrayList<>();
        }
    }

    public static void refreshCachedCustomComponents() {
        cachedCustomComponents = readCustomComponents();
        buildMaps();
        ready = true;
    }

    // =========================
    // VALIDATION
    // =========================

    public static boolean isValidComponent(Map<String, Object> map) {
        return map != null &&
                map.containsKey("name") &&
                map.containsKey("id") &&
                map.containsKey("icon") &&
                map.containsKey("varName") &&
                map.containsKey("typeName") &&
                map.containsKey("buildClass") &&
                map.containsKey("class") &&
                map.containsKey("description") &&
                map.containsKey("url") &&
                map.containsKey("additionalVar") &&
                map.containsKey("defineAdditionalVar") &&
                map.containsKey("imports");
    }

    public static boolean isValidComponentList(List<? extends Map<String, Object>> list) {
        if (list == null) return false;

        for (Map<String, Object> map : list) {
            if (!isValidComponent(map)) return false;
        }

        return true;
    }

    public static Pair<Optional<String>, List<HashMap<String, Object>>> readComponents(String path) {

        String content = FileUtil.readFile(path);

        if (content.isEmpty() || content.equals("[]")) {
            return new Pair<>(Optional.of("Empty file"), Collections.emptyList());
        }

        ArrayList<HashMap<String, Object>> data =
                new Gson().fromJson(content, Helper.TYPE_MAP_LIST);

        if (data == null || data.isEmpty() || !isValidComponentList(data)) {
            return new Pair<>(Optional.of("Invalid JSON"), Collections.emptyList());
        }

        return new Pair<>(Optional.empty(), data);
    }
}