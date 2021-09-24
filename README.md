# SimpleSpringIoc
为了更好的理解springIOC实现原理，看了其他博客分析的IOC源码，结合自己的理解，写了一个简单的Demo

![image](https://user-images.githubusercontent.com/67700636/134616204-0e1f72c1-df6a-4883-be2e-003a467edcf5.png)

如果UserService类依赖了OrderService， 传统我们需要用 'new 类名()' 这种方式来创建一个实例对象，如果是依赖的类比较少，可以选择这种方式。
一旦依赖的对象多了，重复创建实例对象的话，会让我们的代码更难管理。

这时候我们只需要将 对象的创建和管理交给 spring IOC容器，就像自来水一样，需要用我们就去IOC容器当中取出，大大减少了代码量


这是类的结构图

![image](https://user-images.githubusercontent.com/67700636/134616561-9c0be26e-7c7b-493e-be1a-534c44294597.png)


1.先创建两个注解  IocService 和 IocResource


//自定义属性的依赖注入
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IocResource {
}

// 自定义服务的依赖注入
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IocService {
    String name() default  "";

}


2.接着创建两个IService接口， IOrderService 和 IUserService

public interface IUserService {
    String findOrder(String username);
}

public interface IOrderService {
    String findOrder(String username);
}

3.分别创建两个接口实现类

@IocService(name = "useraze")
public class UserService implements IUserService {
    //写的比较简单 这块的属性名称一定要用实现类来命名 且 按照第一个字母要小写的原则 否则很报错的
    @IocResource
    private IOrderService orderService;

    @Override
    public String findOrder(String username) {
        return orderService.findOrder(username);
    }
}

@IocService
public class OrderService implements IOrderService {
    @Override
    public String findOrder(String username) {
        return "用户"+username+"的订单编号是:1001";
    }
}

4.创建一个工具类，获取某个包下所有类的信息


 //获取某个包下面的所有类信息
public class ClassUtils {  
    //  取得某个接口下所有实现这个接口的类
    public static List<Class> getAllClassByInterface(Class c) {
        List<Class> returnClassList = null;

        if (c.isInterface()) {
            // 获取当前的包名
            String packageName = c.getPackage().getName();
            // 获取当前包下以及子包下所以的类
            List<Class<?>> allClass = getClasses(packageName);
            if (allClass != null) {
                returnClassList = new ArrayList<Class>();
                for (Class classes : allClass) {
                    // 判断是否是同一个接口
                    if (c.isAssignableFrom(classes)) {
                        // 本身不加入进去
                        if (!c.equals(classes)) {
                            returnClassList.add(classes);
                        }
                    }
                }
            }
        }

        return returnClassList;
    }

    /*
     * 取得某一类所在包的所有类名 不含迭代
     */
    public static String[] getPackageAllClassName(String classLocation, String packageName) {
        // 将packageName分解
        String[] packagePathSplit = packageName.split("[.]");
        String realClassLocation = classLocation;
        int packageLength = packagePathSplit.length;
        for (int i = 0; i < packageLength; i++) {
            realClassLocation = realClassLocation + File.separator + packagePathSplit[i];
        }
        File packeageDir = new File(realClassLocation);
        if (packeageDir.isDirectory()) {
            String[] allClassName = packeageDir.list();
            return allClassName;
        }
        return null;
    }

    
    // 从包package中获取所有的Class
    // @param packageName
   //  @return
     
    public static List<Class<?>> getClasses(String packageName) {

        // 第一个class类的集合
        List<Class<?>> classes = new ArrayList<Class<?>>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            // 添加到classes
                                            classes.add(Class.forName(packageName + '.' + className));
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

   
   //  以文件的形式获取包下的所有类
   //   @param packageName
    //  @param packagePath
    //  @param recursive
     // @param classes
     

    public static void findAndAddClassesInPackageByFile(String packageName,String packagePath,
                                                        final boolean recursive,List<Class<?>> classes){

        //获取此包的目录，建立一个File
        File dir = new File(packagePath);
        //如果不存在 或者 也不是目录就直接返回
        if (!dir.exists()  || !dir.isDirectory() ){
            return;
        }

        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirFiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            @Override
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });

        // 循环所有文件
        for (File file : dirFiles) {
            //如果是目录，则继续扫描
            if (file.isDirectory()){
                findAndAddClassesInPackageByFile(packageName+"."+file.getName(),file.getAbsolutePath(),recursive,classes);

            }else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    classes.add(Class.forName(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }
    
}


// 初始化bean和bean的字段属性
 
public class SpringContext {
    private String path;
    /*String :beanId Object:serviceimpl*/
    ConcurrentHashMap<String,Object> initBean = null;

    public SpringContext(String path){
        this.path = path;
    }


    /**
     * 根据beanid获取对应的bean
     * @param beanId
     * @return
     * @throws Exception
     */
    public Object getBean(String beanId) throws Exception{
        List<Class> classes = findAnnoationService();
        if (classes == null || classes.isEmpty()) {
            throw new Exception("no found anything bean is useding initial..");
        }
        initBean = initBean(classes);
        if (initBean == null || initBean.isEmpty()) {
            throw new Exception("initial bean is empty or null");
        }
        Object object = initBean.get(beanId);
        //初始化属性的依赖
        initAttribute(object);
        return object;
    }

   
     /* 初始化依赖的属性
      @param object
      @throws IllegalArgumentException
      @throws IllegalAccessException
     */
    private void initAttribute(Object object)throws Exception{
        //获取object的所有类型
        Class<? extends Object> classinfo = object.getClass();
        //获取所有的属性字段
        Field[] fields = classinfo.getDeclaredFields();
        //遍历所有字段
        for(Field field : fields){
            //查找字段上有依赖的注解
            boolean falg = field.isAnnotationPresent(IocResource.class);
            if (falg){
                IocResource iocResource = field.getAnnotation(IocResource.class);
                if (iocResource!=null){
                    //获取属性的beanid
                    String beanId = field.getName();
                    //获取对应的object
                    Object attrObject = getBean(beanId);
                    if (attrObject!=null){
                        //访问私有字段
                        field.setAccessible(true);
                        //赋值
                        field.set(object,attrObject);
                        continue;
                    }
                }
            }
        }
    }

    
      /*初始化bean
      @param classes
      @return
      @throws IllegalAccessException
      @throws InstantiationException
     */
    public ConcurrentHashMap<String,Object> initBean(List<Class>classes) throws IllegalAccessException,InstantiationException{
        ConcurrentHashMap<String,Object> map = new ConcurrentHashMap<String, Object>();
        String beanId="";
        for(Class clazz :classes){
            Object object = clazz.newInstance();
            IocService annotation =(IocService)clazz.getDeclaredAnnotation(IocService.class);
            if (annotation!=null){
                //如果定义了name属性 以实现的name属性为主否则以默认的规则为主
                String value = annotation.name();
                if (value!=null && !value.equals("")){
                    beanId = value;
                }
                else {
                    beanId = toLowerCaseFirstOne(clazz.getSimpleName());
                }
            }

            //存储值
            map.put(beanId,object);
        }
        return map;
    }


    /**
     * 查找包路径下面所有添加注解的类 @IocService
     * @return
     * @throws Exception
     */
    private List<Class> findAnnoationService() throws Exception{
        if (path==null || path.equals("")){
            throw new Exception("scan package address is null or empty..");
        }
        //获取包下面所有的类
        List<Class<?>> classes = ClassUtils.getClasses(path);
        if (classes==null || classes.size()==0){
            throw new Exception("not found service is added annoation for @iocservice");
        }
        List<Class> annoationClasses = new ArrayList<Class>();
        for (Class clazz:classes){
            //通过反射机制 查找增加了注解的类
            IocService iocService = (IocService) clazz.getDeclaredAnnotation(IocService.class);
            if (iocService!=null){
                annoationClasses.add(clazz);
                continue;
            }
        }
        return annoationClasses;
    }


    /**
     * 首字母转换为小写
     * @param s
     * @return
     */
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))){
            return s;
        }
        else{
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }
 }


创建一个测试类

public class SpringIocTest {
    public static void main(String[] args) throws Exception {
        String path = "com.spring.ioc.service.impl";
        SpringContext context = new SpringContext(path);

         IUserService userService = (IUserService) context.getBean("useraze");
        System.out.println(userService.findOrder("xmz"));
      }
}

![image](https://user-images.githubusercontent.com/67700636/134621942-e15fe6d5-9b6e-496b-948a-2e62dccd1056.png)









