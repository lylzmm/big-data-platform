package udf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrainTicket {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("input/火车票.csv")));
        String line = null;
        while ((line = br.readLine()) != null) {
//            String borrowing = extract(line,"");
//            System.out.println(borrowing);
        }
        br.close();
    }

    // 提取火车票数据代码
    public static String extract(String line, String colDate) {
        // 验证是否是火车票信息
        String shuttleBus = validation(line);
        // 如果是提取获取火车编号
        if (shuttleBus.length() == 0) return "";

        // 提取出发站和终点站
        String stand = getStationForAndDestination(line);
        String start_off = "";
        String terminus = "";
        if (stand.split(",").length == 2) {
            start_off = stand.split(",")[0];
            terminus = stand.split(",")[1];
        } else if (stand.split(",").length == 1) {
            start_off = stand.split(",")[0];
        }


        // 获取短信来源
        String source = matchingSource(line);

        // 出行方式
        String wayToTravel = "";

        // 飞机出行
        if (line.contains("起飞")) wayToTravel = "飞机";
        else wayToTravel = "火车";

        // 获取出行时间
        String date = getDate(line);
        // 有些时间 2月18
        // 格式化时间
        if (date.split("-").length == 2) {
            date = colDate.substring(0, 4) + "-" + date;
        }

        // 火车票状态，成功 1 ，失败 0
        String status = "1";
        if (line.contains("中断") || line.contains("失败") || line.contains("未完成") || line.contains("未支付")) status = "0";


        // 开源 | 出行方式 | 时间 | 列车编号 | 出发站和终点站
        return source + "|" + wayToTravel + "|" + date + "|" + shuttleBus + "|" + stand + "|" + status + "|" + start_off + "|" + terminus;
    }

    public static String addZero(String col) {
        if (col.length() < 2) {
            return "0" + col;
        }
        return col;
    }

    // 格式化日期
    public static String formatting(String dueDate) {
        String result = "";
        if (dueDate.contains("年") && dueDate.contains("月") && dueDate.contains("日")) {
            int indexYear = dueDate.indexOf("年");
            int indexMonth = dueDate.indexOf("月");
            int indexDay = dueDate.indexOf("日");

            System.out.println(dueDate);
            String year = dueDate.substring(0, indexYear);
            String month = addZero(dueDate.substring(indexYear + 1, indexMonth));
            String day = addZero(dueDate.substring(indexMonth + 1, indexDay));
            result = year + "-" + month + "-" + day;

        } else if (dueDate.contains("月") && dueDate.contains("日")) {
            int indexMonth = dueDate.indexOf("月");
            int indexDay = dueDate.indexOf("日");
            String month = addZero(dueDate.substring(0, indexMonth));
            String day = addZero(dueDate.substring(indexMonth + 1, indexDay));
            result = month + "-" + day;
        }
        return result;
    }


    // 提取出发站和终点站
    public static String getStationForAndDestination(String line) {
        // 获取出发，终点站
        Set<String> strings = VerifyLocation(line);
        // 去重
        List<String> arr = duplicate(strings);

        // 站台在字符串中排序
        String sort = sort(arr, line);

        String[] split = sort.split(",");
        // 出发站在前，终点站在后
        // 站台关键字合并如：
        // 站台组合有
        // 西安,咸阳,安庆, 天柱山  => 西安咸阳, 安庆天柱山
        // 合肥,安庆, 天柱山      =>  合肥,    安庆天柱山
        if (split.length == 3 || split.length == 4) {
            HashSet<String> set = new HashSet<>();
            for (int i = 0; i < split.length; i++) {
                for (int j = i + 1; j < split.length; j++) {
                    String k1 = split[i].trim();
                    String k2 = split[j].trim();
                    String stand = k1 + k2;
                    if (line.contains(stand)) set.add(stand);
                }
            }
            ArrayList<String> stands = new ArrayList<>(set);
            // 合肥, 安庆, 天柱山      =>    安庆天柱山
            if (set.size() == 1) {
                for (String key : split) {
                    for (String stand : set) {
                        if (!stand.contains(key.trim())) stands.add(key);
                    }
                }
            }
            return sort(stands, line);
        }
        return sort;
    }

    // 获取时间
    public static String getDate(String line) {
        String date = "";
        if (line.contains("起飞")) {
            // **************************************************  获取关键字最近的日期
            // 匹配 2021-07-25 21:40:00
            Pattern p2 = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}");
            Matcher m2 = p2.matcher(line.trim());
            ArrayList<String> list = new ArrayList<>();
            while (m2.find()) list.add(m2.group());

            Map<Integer, String> map = new TreeMap<>();
            TreeSet<Integer> set = new TreeSet<>(Comparator.reverseOrder());
            if (list.size() > 1) {
                int index = line.indexOf("起飞");

                for (String s : list) {
                    int dateIndex = line.indexOf(s);
                    if (dateIndex < index) dateIndex += s.length();
                    int abs = Math.abs(dateIndex - index);
                    // 差值排序
                    set.add(abs);
                    map.put(abs, s);
                }
                date = map.get(set.last());
            } else if (list.size() == 1) {
                date = list.get(0);
            }
            return date;
        } else {
            Pattern p1 = Pattern.compile("\\d{1,2}月\\d{1,2}日");
            Matcher m1 = p1.matcher(line.trim());
            ArrayList<String> list = new ArrayList<>();
            while (m1.find()) list.add(m1.group());

            if (list.size() >= 1) return formatting(list.get(0));
            return "";
        }

    }

    // 出发站在前，终点站在后
    public static String sort(List<String> list, String line) {
        TreeMap<Integer, String> map = new TreeMap<Integer, String>(Integer::compareTo);
        for (String s : list) {
            map.put(line.indexOf(s), s);
        }

        return map.values().toString().replace("[", "").replace("]", "");
    }

    // 去重
    public static List<String> duplicate(Set<String> set2) {
        ArrayList<String> list = new ArrayList<>(set2);
        // 要去重的元素
        HashSet<String> rmSet = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                String k1 = list.get(i);
                String k2 = list.get(j);
                // 蚌埠南 蚌埠
                if (k1.length() > k2.length()) if (k1.contains(k2)) rmSet.add(k2);
                if (k1.length() < k2.length()) if (k2.contains(k1)) rmSet.add(k1);
            }
        }

        // 去重后的集合
        ArrayList<String> newList = new ArrayList<>();
        for (String s : list) if (!rmSet.contains(s)) newList.add(s);
        return newList;
    }


    // 提取火车去向关键字
    public static Set<String> VerifyLocation(String line) {
        List<String> strings = Arrays.asList("北京北", "北京东", "北京", "北京南", "北京西", "广州南", "重庆北", "重庆", "重庆南", "重庆西", "广州东", "上海", "上海南", "上海虹桥", "上海西", "天津北", "天津", "天津南", "天津西", "香港西九龙", "长春", "长春南", "长春西", "成都东", "成都南", "成都", "成都西", "长沙", "长沙南", "大明湖", "福州", "福州南", "贵阳", "广州", "广州西", "哈尔滨", "哈尔滨东", "哈尔滨西", "合肥", "呼和浩特东", "呼和浩特", "海口东", "海口", "杭州东", "杭州", "杭州南", "济南", "济南", "济南西", "济南西", "昆明", "昆明西", "拉萨", "兰州东", "兰州", "兰州西", "南昌", "南京", "南京南", "南宁", "石家庄北", "石家庄", "蜀山东", "沈阳", "沈阳北", "沈阳东", "沈阳南", "太原北", "太原东", "太原", "武汉", "王家营西", "乌鲁木齐", "西安北", "西安", "西安南", "西宁", "银川", "郑州", "阿尔山", "安康", "阿克苏", "阿里河", "阿拉山口", "安平", "安庆", "安顺", "鞍山", "安阳", "北安", "蚌埠", "白城", "北海", "白河", "宝鸡", "白涧", "滨江", "博克图", "百色", "白山市", "北台", "包头东", "包头", "北屯市", "本溪", "白云鄂博", "白银西", "亳州", "赤壁", "常德", "承德", "长甸", "赤峰南", "茶陵", "苍南", "昌平", "崇仁", "昌图", "长汀镇", "曹县", "楚雄南", "陈相屯", "长治北", "池州", "长征", "常州", "郴州", "长治", "沧州", "崇左", "大安北", "大成", "丹东", "东方红", "东莞东", "大虎山", "敦化", "敦煌", "德惠", "东京城", "大涧", "都江堰", "大连北", "大理", "大连", "定南", "大庆", "东胜", "大石桥", "大同", "东营", "大杨树", "都匀", "达州", "邓州", "德州", "额济纳", "二连", "恩施", "福鼎", "凤凰机场", "风陵渡", "涪陵", "富拉尔基", "抚顺北", "佛山", "阜新南", "阜阳", "格尔木", "广汉", "古交", "桂林北", "桂林", "古莲", "固始", "广水", "干塘", "广元", "广州北", "赣州", "公主岭", "公主岭南", "淮安", "淮北", "鹤北", "淮滨", "河边", "韩城", "潢川", "邯郸", "横道河子", "鹤岗", "皇姑屯", "红果", "黑河", "怀化", "汉口", "葫芦岛", "海拉尔", "霍林郭勒", "海伦", "侯马", "哈密", "淮南", "桦南", "海宁西", "鹤庆", "怀柔北", "怀柔", "黄石东", "黄山", "黄石", "衡水", "衡阳", "菏泽", "贺州", "汉中", "惠州", "吉安", "集安", "江边村", "晋城", "金城江", "景德镇", "嘉峰", "加格达奇", "井冈山", "蛟河", "金华南", "金华", "九江", "吉林", "荆门", "佳木斯", "济宁", "集宁南", "酒泉", "江山", "吉首", "九台", "镜铁山", "鸡西", "绩溪县", "嘉峪关", "江油", "蓟州北", "锦州", "金州", "库尔勒", "开封", "岢岚", "凯里", "喀什", "昆山南", "奎屯", "开原", "六安", "灵宝", "芦潮港", "陆川", "利川", "临川", "隆昌", "潞城", "鹿道", "娄底", "临汾", "良各庄", "临河", "漯河", "绿化", "隆化", "龙井", "丽江", "临江", "吕梁", "醴陵", "柳林南", "滦平", "六盘水", "灵丘", "旅顺", "陇西", "澧县", "兰溪", "临西", "龙岩", "耒阳", "洛阳", "连云港东", "洛阳东", "临沂", "洛阳龙门", "凌源", "柳园", "辽源", "柳州", "立志", "辽中", "麻城", "免渡河", "牡丹江", "莫尔道嘎", "满归", "明光", "漠河", "茂名", "茂名西", "密山", "马三家", "麻尾", "绵阳", "孟塬", "梅州", "满洲里", "宁波东", "宁波", "南充", "南岔", "南丹", "南大庙", "南芬", "讷河", "内江", "嫩江", "南通", "南阳", "碾子山", "平顶山", "盘锦", "平凉", "平凉南", "平泉", "坪石", "凭祥", "萍乡", "郫县西", "攀枝花", "蕲春", "青城山", "青岛", "清河城", "黔江", "曲靖", "前进镇", "齐齐哈尔", "七台河", "沁县", "泉州东", "泉州", "衢州", "融安", "汝箕沟", "瑞金", "日照", "双城堡", "绥芬河", "韶关东", "山海关", "绥化", "三间房", "苏家屯", "舒兰", "神木南", "三门峡", "商南", "遂宁", "四平", "商丘", "上饶", "韶山", "宿松", "汕头", "邵武", "涉县", "三亚", "邵阳", "十堰", "三元区", "双鸭山", "松原", "宿州", "苏州", "深圳", "随州", "朔州", "深圳西", "塘豹", "塔尔气", "潼关", "塘沽", "塔河", "通化", "泰来", "吐鲁番", "通辽", "铁岭", "陶赖昭", "图们", "铜仁", "唐山北", "田师府", "泰山", "天水", "唐山", "通远堡", "太阳升", "泰州", "通州西", "武昌", "五常", "瓦房店", "威海", "芜湖", "乌海西", "吴家屯", "乌鲁木齐南", "武隆", "乌兰浩特", "渭南", "威舍", "歪头山", "武威", "武威南", "无锡", "乌西", "乌伊岭", "武夷山", "万源", "万州", "梧州", "温州", "温州南", "西昌", "许昌", "西昌南", "锡林浩特", "厦门北", "厦门", "厦门高崎", "宣威", "新乡", "信阳", "咸阳", "襄阳", "熊岳城", "新余", "徐州", "延安", "宜宾", "亚布力南", "叶柏寿", "宜昌东", "盐城", "运城", "伊春", "宜昌", "榆次", "杨村", "宜春西", "伊尔施", "燕岗", "延吉", "永济", "营口", "牙克石", "玉林", "榆林", "阎良", "亚龙湾", "一面坡", "伊宁", "阳平关", "玉屏", "原平", "延庆", "阳泉曲", "阳泉", "玉泉", "玉山", "营山", "燕山", "榆树", "鹰潭", "烟台", "伊图里河", "玉田县", "义乌", "阳新", "义县", "益阳", "岳阳", "崖州", "扬州", "永州", "淄博", "镇城底", "自贡北", "珠海", "珠海北", "湛江", "镇江", "张家界", "张家口", "周口", "扎兰屯", "驻马店", "肇庆", "周水子", "昭通", "中卫", "资阳", "遵义西", "枣庄", "资中", "株洲", "枣庄西", "昂昂溪", "阿城", "安达", "安德", "安定", "安多", "安广", "敖汉", "艾河", "安化", "艾家村", "阿金", "安靖", "安家", "阿克陶", "安口窑", "敖力布告", "安龙", "阿龙山", "安陆", "阿木尔", "阿南庄", "鞍山西", "安塘", "安亭北", "阿图什", "安图", "安溪", "博鳌", "白壁关", "蚌埠南", "巴楚", "板城", "北戴河", "保定", "宝坻", "八达岭", "巴东", "宝丰", "柏果", "布海", "白河东", "宝华山", "白河县", "白芨沟", "碧鸡关", "北滘", "碧江", "白鸡坡", "笔架山", "八角台", "保康", "白奎堡", "白狼", "百浪", "博乐", "巴林", "北流", "勃利", "宝林", "布列开", "宝龙山", "百里峡", "八面城", "班猫箐", "八面通", "北马圈子", "北票南", "白旗", "宝泉岭", "白泉", "巴山", "白水江", "白沙坡", "白石山", "白水镇", "包头东", "坂田", "泊头", "北屯", "本溪湖", "博兴", "八仙筒", "白音察干", "背荫河", "北营", "巴彦高勒", "白音他拉", "鲅鱼圈", "白银市", "白音胡硕", "巴中", "霸州", "北宅", "赤壁北", "查布嘎", "长城", "长冲", "承德东", "赤峰", "嵯岗", "柴岗", "长葛", "柴沟堡", "城固", "陈官营", "成高子", "草海", "册亨", "柴河", "草河口", "崔黄口", "巢湖", "蔡家沟", "成吉思汗", "岔江", "蔡家坡", "昌乐", "超梁沟", "慈利", "昌黎", "长岭子", "晨明", "长农", "昌平北", "常平", "长坡岭", "辰清", "蔡山", "长寿", "磁山", "苍石", "草市", "楚山", "察素齐", "长山屯", "长汀", "朝天南", "昌图西", "春湾", "岑溪", "辰溪", "磁县", "磁西", "长兴南", "磁窑", "春阳", "城阳", "朝阳川", "创业村", "朝阳地", "朝阳南", "长垣", "朝阳镇", "滁州北", "常州北", "滁州", "潮州", "常庄", "曹子里", "车转湾", "郴州西", "沧州西", "德安", "大安", "大坝", "电白", "大板", "到保", "大巴", "达坂城", "定边", "东边井", "德伯斯", "打柴沟", "德昌", "滴道", "大磴沟", "刀尔登", "得耳布尔", "杜尔伯特", "东方", "丹凤", "东丰", "都格", "大官屯", "大关", "东光", "东海", "大灰厂", "大红旗", "大禾塘", "东海县", "德惠西", "达家沟", "东津", "杜家", "大口屯", "东来", "德令哈", "大林", "带岭", "达拉特旗", "独立屯", "豆罗", "达拉特西", "大连西", "东明村", "洞庙河", "东明县", "大拟", "大平房", "大盘石", "大埔", "大堡", "大庆东", "大其拉哈", "道清", "对青山", "德清西", "大庆西", "东升", "砀山", "独山", "登沙河", "读书铺", "大石头", "东胜西", "大石寨", "东台", "定陶", "灯塔", "大田边", "东通化", "丹徒", "大屯", "东湾", "大武口", "低窝铺", "大王滩", "大湾子", "大兴沟", "定西", "东乡", "定襄", "代县", "大兴", "甸心", "东戌", "东辛庄", "大雁", "德阳", "丹阳", "当阳", "丹阳北", "大英东", "东淤地", "大营", "定远", "岱岳", "大元", "大营子", "大营镇", "大战场", "德州东", "东至", "兑镇", "道州", "低庄", "东镇", "东庄", "定州", "豆庄", "大竹园", "大杖子", "豆张庄", "峨边", "二道沟门", "二道湾", "鄂尔多斯", "二龙", "二龙山屯", "峨眉", "二密河", "恩平", "二营", "鄂州", "福安", "丰城", "丰城南", "肥东", "发耳", "福海", "富海", "凤凰城", "汾河", "奉化", "富锦", "范家屯", "福利区", "福利屯", "丰乐镇", "阜南", "阜宁", "抚宁", "福清", "福泉", "丰水村", "丰顺", "繁峙", "抚顺", "福山口", "扶绥", "冯屯", "浮图峪", "富县东", "凤县", "费县", "富县", "肥西", "汾阳", "凤阳", "扶余北", "分宜", "扶余", "富源", "富裕", "抚州北", "丰镇", "范镇", "凤州", "广安", "固安", "高碑店", "沟帮子", "甘草店", "谷城", "藁城", "高村", "古城镇", "广德", "贵定", "古东", "贵港", "官高", "葛根庙", "干沟", "甘谷", "高各庄", "根河", "甘河", "郭家店", "孤家子", "古浪", "皋兰", "高楼房", "归流河", "关林", "甘洛", "郭磊庄", "高密", "公庙子", "工农湖", "广宁寺南", "广南卫", "高平", "甘泉北", "共青城", "甘旗卡", "甘泉", "高桥镇", "灌水", "孤山口", "果松", "嘎什甸子", "高山子", "高台", "高滩", "古田", "官厅", "官厅西", "贵溪", "涡阳", "巩义", "高邑", "广元南", "巩义南", "固原", "菇园", "公营子", "光泽", "古镇", "固镇", "瓜州", "虢镇", "高州", "盖州", "官字井", "冠豸山南", "盖州西", "海安", "淮安南", "红安", "红安西", "黄柏", "海北", "鹤壁", "会昌北", "合川", "华城", "河唇", "汉川", "海城", "黑冲滩", "黄村", "海城西", "化德", "洪洞", "霍尔果斯", "横峰", "韩府湾", "汉沽", "黄瓜园", "红光镇", "浑河", "红花沟", "黄花筒", "贺家店", "和静", "涵江", "获嘉", "河津", "黑井", "红江", "华家", "杭锦后旗", "河间西", "花家庄", "河口南", "黄口", "湖口", "呼兰", "葫芦岛北", "浩良河", "哈拉海", "黄陵", "寒岭", "鹤立", "海林", "桦林", "虎林", "和龙", "海龙", "哈拉苏", "呼鲁斯太", "火连寨", "黄梅", "韩麻营", "黄泥河", "海宁", "怀宁", "惠农", "和平", "花棚子", "花桥", "宏庆", "怀仁", "华容", "华山北", "黄松甸", "和什托洛盖", "汉寿", "惠山", "衡山", "黑水", "红山", "虎什哈", "红寺堡", "虎石台", "海石湾", "红砂岘", "衡山西", "桓台", "黑台", "和田", "会同", "海坨子", "黑旺", "海湾", "徽县", "红星", "红兴隆", "红岘台", "换新天", "红彦", "海晏", "合阳", "衡阳东", "华蓥", "汉阴", "黄羊滩", "汉源", "河源", "花园", "湟源", "黄羊镇", "湖州", "黄州", "化州", "霍州", "惠州西", "巨宝", "靖边", "金宝屯", "晋城北", "交城", "金昌", "建昌", "鄄城", "峻德", "井店", "鸡东", "江都", "鸡冠山", "金沟屯", "静海", "金河", "精河", "锦河", "精河南", "建湖", "江华", "纪家沟", "晋江", "锦界", "姜家", "金坑", "芨岭", "金马村", "江门东", "角美", "江门", "莒南", "井南", "建瓯", "经棚", "金普", "江桥", "九三", "金山北", "建始", "嘉善", "京山", "稷山", "吉舒", "甲山", "建设", "建三江", "嘉善南", "金山屯", "江所田", "景泰", "九台南", "吉文", "介休", "莒县", "嘉祥", "嘉兴", "进贤", "井陉", "嘉兴南", "夹心子", "建阳", "姜堰", "简阳", "巨野", "揭阳南", "江永", "缙云", "靖远", "江源", "济源", "靖远西", "胶州北", "焦作东", "金寨", "荆州", "靖州", "胶州", "晋州", "蓟州", "锦州南", "焦作", "旧庄窝", "金杖子", "开安", "库车", "康城", "库都尔", "宽甸", "克东", "昆都仑召", "开江", "康金井", "喀喇其", "开鲁", "克拉玛依", "开平南", "口前", "昆山", "奎山", "克山", "康熙岭", "昆阳", "克一河", "开原西", "康庄", "来宾", "老边", "灵宝西", "洛川东", "龙川", "乐昌", "聊城", "黎城", "蓝村", "两当", "林东", "乐都", "梁底下", "六道河子", "廊坊", "鲁番", "落垡", "廊坊北", "老府", "兰岗", "龙骨甸", "龙沟", "芦沟", "拉古", "临海", "凌海", "拉哈", "林海", "柳河", "六合", "龙华", "滦河沿", "六合镇", "亮甲店", "刘家店", "刘家河", "廉江", "罗江", "两家", "李家", "龙江", "连江", "庐江", "龙嘉", "莲江口", "蔺家楼", "李家坪", "兰考", "林口", "路口铺", "老莱", "临澧", "陆良", "零陵", "龙里", "兰棱", "拉林", "卢龙", "喇嘛甸", "里木店", "洛门", "龙南", "梁平", "罗平", "落坡岭", "六盘山", "乐平市", "临清", "龙泉寺", "乐山北", "乐善村", "冷水江东", "连山关", "流水沟", "丽水", "灵石", "陵水", "罗山", "露水河", "鲁山", "庐山", "梁山", "林盛堡", "柳树屯", "龙山镇", "梨树镇", "李石寨", "轮台", "黎塘", "芦台", "龙塘坝", "濑湍", "骆驼巷", "李旺", "莱芜东", "狼尾山", "灵武", "莱芜西", "芦溪", "陇县", "临湘", "林西", "滦县", "朗乡", "良乡", "莱西南", "略阳", "莱阳", "辽阳", "凌源东", "临沂东", "连云港", "老营", "临颍", "龙游", "涟源", "涞源", "林源", "罗源", "耒阳西", "临泽", "龙爪沟", "雷州", "来舟", "六枝", "龙镇", "拉鲊", "鹿寨", "兰州新区", "马鞍山", "毛坝", "毛坝关", "麻城北", "渑池", "明城", "庙城", "渑池南", "茅草坪", "猛洞河", "磨刀石", "弥渡", "帽儿山", "明港", "梅河口", "马皇", "孟家岗", "美兰", "汨罗东", "马莲河", "庙岭", "茅岭", "麻柳", "马林", "茂林", "穆棱", "马龙", "木里图", "汨罗", "玛纳斯湖", "冕宁", "沐滂", "马桥河", "闽清", "民权", "眉山", "明水河", "麻山", "米沙子", "茂舍祖", "马踏", "勉县", "美溪", "麻阳", "密云北", "米易", "墨玉", "麦园", "庙庄", "米脂", "明珠", "宁安", "农安", "南博山", "南仇", "南城司", "宁村", "宁德", "南观村", "南宫东", "南关岭", "宁国", "宁海", "南华北", "南河川", "泥河子", "南靖", "宁家", "能家", "牛家", "南口", "南口前", "南朗", "乃林", "尼勒克", "那罗", "宁陵县", "奈曼", "宁明", "南木", "那铺", "南桥", "那曲", "暖泉", "南台", "南头", "宁武", "南湾子", "南翔北", "内乡", "宁乡", "牛心台", "南峪", "娘子关", "南召", "南杂木", "蓬安", "平安", "平安驿", "磐安镇", "平安镇", "蒲城东", "蒲城", "裴德", "偏店", "坡底下", "瓢儿屯", "平房", "平岗", "平果", "平关", "盘关", "徘徊北", "平河口", "平湖", "盘锦北", "潘家店", "皮口南", "普兰店", "偏岭", "彭山", "皮山", "彭水", "平社", "磐石", "平山", "平台", "莆田", "平田", "葡萄菁", "平旺", "平型关", "蓬溪", "普雄", "郫县", "平遥", "彭阳", "平阳", "平洋", "平邑", "平原堡", "平原", "平峪", "平庄北", "彭泽", "邳州", "泡子", "平庄南", "乾安", "迁安", "庆安", "祁东北", "七甸", "曲阜东", "庆丰", "奇峰塔", "曲阜", "琼海", "秦皇岛", "千河", "清河", "清河门", "清华园", "全椒", "渠旧", "潜江", "秦家", "祁家堡", "清涧县", "秦家庄", "七里河", "渠黎", "秦岭", "青龙", "青龙山", "祁门", "前磨头", "清水", "前山", "确山", "青山", "戚墅堰", "青田", "桥头", "青铜峡", "前卫", "前苇塘", "祁县", "渠县", "青县", "桥西", "清徐", "旗下营", "泉阳", "千阳", "沁阳", "祁阳北", "七营", "庆阳山", "清远", "清原", "钦州东", "钦州", "青州市", "瑞安", "瑞昌", "如皋", "容桂", "任丘", "融水", "乳山", "热水", "容县", "饶阳", "汝阳", "绕阳河", "汝州", "石坝", "上板城", "施秉", "上板城南", "世博园", "双城北", "舒城", "莎车", "商城", "顺昌", "神池", "沙城", "石城", "山城镇", "山丹", "绥德", "顺德", "水洞", "商都", "十渡", "四道湾", "顺德学院", "绅坊", "双丰", "四方台", "水富", "三关口", "桑根达来", "韶关", "上高镇", "沙海", "上杭", "蜀河", "松河", "沙河", "沙河口", "赛汗塔拉", "沙后所", "沙河市", "山河屯", "三河县", "四合永", "石河子", "双河镇", "三汇镇", "三合庄", "三家店", "松江河", "沈家河", "水家湖", "双吉", "尚家", "松江", "孙家", "沈家", "三江口", "司家岭", "松江南", "石景山南", "邵家堂", "三江县", "松江镇", "三家寨", "十家子", "深井子", "施家嘴", "什里店", "疏勒", "舍力虎", "疏勒河", "双辽", "石岭", "石磷", "绥棱", "石林", "石林南", "石龙", "萨拉齐", "商洛", "索伦", "沙岭子", "石门县北", "三门峡南", "三门县", "石门县", "神木西", "三门峡西", "肃宁", "宋", "双牌", "沙坪坝", "四平东", "遂平", "沙坡头", "沙桥", "商丘南", "水泉", "石泉县", "石桥子", "石人城", "石人", "鄯善", "三水", "泗水", "松树", "山市", "神树", "石山", "首山", "三十家", "三十里堡", "双水镇", "松树镇", "松桃", "索图罕", "三堂集", "神头", "石头", "沙沱", "上万", "孙吴", "沙湾县", "遂溪", "石岘", "沙县", "绍兴", "歙县", "上西铺", "石峡子", "沭阳", "寿阳", "绥阳", "水洋", "三阳川", "上腰墩", "三营", "顺义", "三义井", "三源浦", "上园", "上虞", "三原", "水源", "桑园子", "绥中北", "苏州北", "宿州东", "深圳东", "孙镇", "绥中", "深州", "师庄", "尚志", "松滋", "师宗", "苏州园区", "苏州新区", "泰安", "台安", "通安驿", "桐柏", "通北", "桐城", "郯城", "铁厂", "汤池", "桃村", "通道", "田东", "天岗", "土贵乌拉", "通沟", "太谷", "塔哈", "棠海", "泰和", "唐河", "太湖", "团结", "谭家井", "陶家屯", "唐家湾", "统军庄", "吐列毛杜", "图里河", "铜陵", "亭亮", "田林", "铁力", "铁岭西", "图们北", "天门", "天门南", "太姥山", "土牧尔台", "土门子", "潼南", "洮南", "太平川", "太平镇", "台前", "图强", "天桥岭", "土桥子", "汤山城", "台山", "桃山", "塔石嘴", "通途", "汤旺河", "同心", "桐乡", "土溪", "田阳", "汤阴", "天义", "驼腰岭", "太阳山", "通榆", "汤原", "塔崖驿", "滕州东", "滕州", "天祝", "天镇", "桐子林", "天柱山", "台州西", "武安", "文安", "王安镇", "吴堡", "旺苍", "五叉沟", "文昌", "温春", "五大连池", "文登", "五道沟", "五道河", "文地", "卫东", "武当山", "望都", "乌尔旗汗", "潍坊", "万发屯", "王府", "瓦房店西", "王岗", "湾沟", "武功", "吴官田", "乌海", "苇河", "卫辉", "吴家川", "威箐", "午汲", "渭津", "五家", "王家湾", "倭肯", "五棵树", "五龙背", "万乐", "瓦拉干", "温岭", "五莲", "乌拉特前旗", "卧里屯", "渭南北", "乌奴耳", "万宁", "万年", "渭南南", "渭南镇", "沃皮", "汪清", "吴桥", "武清", "文水", "武山", "魏善庄", "王瞳", "五台山", "王团庄", "五五", "无锡东", "武乡东", "闻喜", "卫星", "无锡新区", "武穴", "吴圩", "王杨", "武义", "五营", "瓦窑田", "五原", "苇子沟", "韦庄", "五寨", "王兆屯", "魏杖子", "微子镇", "新安", "兴安", "新安县", "新保安", "下板城", "西八里", "宣城", "兴城", "小村", "新绰源", "下城子", "新城子", "喜德", "小得江", "西大庙", "小董", "小东", "香坊", "信丰", "襄汾", "息烽", "新干", "孝感", "轩岗", "西固城", "兴国", "西固", "夏官营", "西岗子", "宣汉", "新和", "宣和", "襄河", "斜河涧", "新华屯", "新会", "新华", "新化", "新晃", "宣化", "兴和西", "小河沿", "下花园", "小河镇", "徐家店", "峡江", "新绛", "新江", "辛集", "徐家", "西街口", "许家屯", "许家台", "谢家镇", "兴凯", "小榄", "香兰", "兴隆店", "新乐", "新李", "西柳", "西林", "新林", "仙林", "小岭", "新立屯", "兴隆县", "新立镇", "兴隆镇", "新民", "西麻山", "下马塘", "孝南", "咸宁北", "兴宁", "咸宁", "犀浦东", "兴平", "西平", "新坪田", "霞浦", "溆浦", "犀浦", "新邱", "新青", "兴泉堡", "仙人桥", "小寺沟", "浠水", "秀山", "杏树", "夏石", "下社", "徐水", "小市", "小哨", "新松浦", "杏树屯", "许三湾", "湘潭", "邢台", "向塘", "仙桃西", "下台子", "徐闻", "新窝铺", "修武", "孝西", "新县", "息县", "西乡", "湘乡", "西峡", "小新街", "新兴县", "西小召", "小西庄", "旬阳", "向阳", "旬阳北", "兴业", "小雨谷", "兴义", "新沂", "信宜", "小月旧", "小扬气", "襄垣", "祥云西", "夏邑县", "新友谊", "新阳镇", "徐州东", "新帐房", "襄州", "忻州", "新肇", "悬钟", "汐子", "西哲里木", "新杖子", "依安", "姚安", "永安", "永安乡", "亚布力", "元宝山", "羊草", "秧草地", "叶城", "盐池", "砚川", "阳春", "阳澄湖", "应城", "宜城", "郓城", "晏城", "禹城", "阳岔", "迎春", "阳城", "雁翅", "云彩岭", "虞城县", "营城子", "英德", "永登", "永定", "尹地", "阳东", "雁荡山", "于都", "园墩", "英德西", "永丰营", "阳高", "杨岗", "阳谷", "友好", "余杭", "沿河城", "岩会", "羊臼河", "盐津", "阳江", "永嘉", "余江", "燕郊", "营街", "姚家", "岳家井", "一间堡", "英吉沙", "云居寺", "燕家庄", "永康", "营口东", "永郎", "银浪", "宜良北", "永乐店", "伊拉哈", "杨陵", "伊林", "杨林", "余粮堡", "杨柳青", "月亮田", "义马", "阳明堡", "玉门", "云梦", "元谋", "一面山", "沂南", "宜耐", "伊宁东", "营盘水", "羊堡", "阳泉北", "焉耆", "乐清", "源迁", "姚千户屯", "阳曲", "榆树沟", "榆社", "玉石", "偃师", "月山", "颍上", "沂水", "玉舍", "窑上", "元氏", "杨树岭", "野三坡", "榆树屯", "榆树台", "鹰手营子", "源潭", "牙屯堡", "烟筒山", "烟筒屯", "羊尾哨", "阳西", "越西", "攸县", "永修", "玉溪西", "弋阳", "酉阳", "余姚", "岳阳东", "阳邑", "鸭园", "鸳鸯镇", "燕子砭", "宜州", "仪征", "兖州", "迤资", "羊者窝", "杨杖子", "镇安", "治安", "招柏", "张百湾", "中川机场", "子长", "枝城", "邹城", "诸城", "赵城", "章党", "正定", "肇东", "照福铺", "章古台", "赵光", "中和", "中华门", "枝江北", "钟家村", "朱家沟", "紫荆关", "诸暨", "周家", "镇江南", "周家屯", "褚家湾", "湛江西", "朱家窑", "张兰", "镇赉", "枣林", "扎鲁特", "扎赉诺尔西", "樟木头", "中牟", "中宁东", "中宁", "中宁南", "镇平", "漳平", "泽普", "张桥", "枣强", "章丘", "朱日和", "泽润里", "中山北", "樟树东", "柞水", "中山", "樟树", "珠斯花", "钟山", "珠窝", "张维屯", "彰武", "资溪", "钟祥", "镇西", "棕溪", "张辛", "正镶白旗", "紫阳", "枣阳", "竹园坝", "张掖", "镇远", "漳州东", "漳州", "子洲", "涿州", "中寨", "壮志", "咋子", "卓资山", "株洲西", "郑州西", "阿巴嘎旗", "阿城北", "阿尔山北", "安江东", "安吉", "安匠", "阿克塞", "阿勒泰", "安陆西", "安仁", "安顺西", "安图西", "安亭西", "安阳东", "博白", "八步", "栟茶", "八达岭长城", "保定东", "博尔塔拉", "八方山", "白沟", "滨海", "滨海北", "滨海港", "滨海西", "毕节", "宝鸡南", "北京大兴", "北京朝阳", "北井子", "八里甸子", "白马北", "白马井", "北票", "宝清", "璧山", "白沙铺", "白水县", "板塘", "巴图营", "白文东", "宾西北", "本溪新城", "步行街", "宾阳", "白云北", "白云机场北", "白洋淀", "宝应", "百宜", "白音华南", "霸州北", "巴中东", "彬州东", "滨州", "彬州", "宾州", "亳州南", "霸州西", "长安", "长安西", "澄城", "承德县北", "成都东", "承德南", "曹妃甸东", "曹妃甸港", "城固北", "长葛北", "查干湖", "巢湖东", "从江", "蔡家崖", "茶卡", "长乐东", "长乐", "长临河", "长流", "茶陵南", "长乐南", "长宁", "常平东", "常平南", "长箐", "长庆桥", "重庆西", "长寿北", "长寿湖", "潮汕", "常山", "常熟", "长沙西", "朝天", "长汀南", "长武", "长兴", "楚雄", "苍溪", "城西", "潮阳", "长阳", "朝阳湖", "昌邑", "长治东", "陈庄", "崇州", "长治南", "城子坦", "东安东", "德保", "东岔", "都昌", "东城南", "东戴河", "丹东西", "东二道河", "大方", "大丰", "大方南", "东港北", "东莞港", "大港南", "大孤山", "东莞", "东莞西", "东花园北", "鼎湖东", "鼎湖山", "垫江", "道滘", "洞井", "丹江口", "董家口", "大苴", "洞口", "达连河", "大荔", "大朗镇", "得莫利", "大青沟", "德清", "杜桥", "东胜东", "大石头南", "砀山南", "当涂东", "大同南", "大通西", "大旺", "定西北", "大兴机场", "德兴东", "德兴", "丹霞山", "大阳", "大冶北", "都匀东", "大邑", "东营南", "大余", "邓州东", "定州东", "端州", "大足南", "额敏", "峨眉山", "阿房宫", "鄂州东", "防城港北", "丰城东", "凤城东", "富川", "方城", "繁昌西", "丰都", "扶沟南", "福海西", "涪陵北", "枫林", "阜宁东", "富宁", "阜宁南", "佛坪", "法启", "芙蓉南", "丰顺东", "复盛", "富顺", "抚松", "佛山西", "福山镇", "福田", "凤台南", "费县北", "阜新", "富阳", "富源北", "富蕴", "抚远", "阜阳西", "抚州东", "抚州", "福州", "方正", "福州南", "固安东", "高安", "广安南", "贵安", "高碑店东", "古北口", "谷城北", "古城东", "恭城", "藁城南", "贵定北", "广德南", "葛店南", "贵定县", "岗嘎", "贡嘎", "广汉北", "高花", "个旧", "革居", "高楞", "关岭", "桂林西", "高密北", "光明城", "灌南", "广宁", "广宁寺", "广南县", "高平东", "桂平", "高坪", "广平", "弓棚子", "赶水东", "光山", "谷山", "观沙岭", "古田北", "广通北", "高台南", "古田会址", "赣县北", "高兴", "高邮北", "贵阳北", "贵阳北", "观音机场", "贵阳东", "高邮", "赣榆", "灌云", "高邑西", "赣州西", "淮安东", "惠安", "怀安", "惠安堡", "淮北北", "鹤壁东", "花博山", "寒葱沟", "珲春", "霍城", "花城街", "河东机场", "邯郸东", "横道河子东", "海东", "惠东", "哈达铺", "花都", "海东西", "洪洞西", "哈尔滨北", "合肥北城", "合肥南", "合肥南", "黄冈", "黄冈东", "横沟桥东", "黄冈西", "洪河", "怀化南", "黄河景区", "惠环", "花湖", "后湖", "怀集", "厚街", "河口北", "宏克力", "怀来", "海林北", "黄流", "黄陵南", "虎门北", "虎门东", "鲘门", "海门", "虎门", "洪梅", "侯马西", "衡南", "淮南东", "淮南南", "合浦", "横琴北", "霍邱", "横琴", "华容东", "怀仁东", "华容南", "怀柔南", "红寺堡北", "黄山北", "黄石北", "衡水北", "黑山北", "贺胜桥东", "和硕", "含山南", "花山南", "黑山寺", "花山镇", "荷塘", "黄桶北", "黄土店", "花土沟", "环县", "合阳北", "海阳北", "槐荫", "鄠邑", "花园口", "淮阳南", "霍州东", "壶镇", "惠州南", "建安", "吉安西", "晋城东", "加查", "泾川", "碱厂", "景德镇北", "旌德", "建德", "尖峰", "近海", "蛟河西", "军粮城北", "将乐", "贾鲁河", "九郎山", "即墨北", "剑门关", "佳木斯西", "建宁县北", "莒南北", "济南东", "江宁", "江宁西", "建瓯东", "建瓯西", "建平", "建桥", "酒泉南", "句容西", "建水", "尖山", "界首南", "界首市", "吉水西", "绩溪北", "介休东", "泾县", "靖西", "郏县", "进贤南", "揭阳", "江油北", "揭阳机场", "嘉峪关南", "简阳南", "金银潭", "靖宇", "金月湾", "缙云西", "锦州北", "晋中", "景州", "焦作西", "库尔木依", "开封北", "开福寺", "开化", "凯里南", "库伦", "昆明南", "葵潭", "开阳", "昆玉", "喀左", "隆安东", "冷坝", "来宾北", "灵璧", "寮步", "绿博园", "临沧", "隆昌北", "乐昌东", "罗城", "陵城", "临城", "老城镇", "龙洞堡", "娄底南", "乐东", "离堆公园", "娄烦", "陆丰", "来凤", "龙丰", "禄丰南", "临汾西", "临高南", "麓谷", "滦河", "珞璜南", "临海南", "凌海南", "隆回", "漯河西", "罗江东", "柳江", "利津南", "庐江西", "厉家寨", "兰考南", "龙口市", "龙里北", "沥林北", "兰陵北", "醴陵东", "芦庙", "陇南", "辽宁朝阳", "六盘水东", "梁平南", "龙桥", "礼泉南", "龙泉市", "礼泉", "临泉", "龙山北", "灵石东", "乐山", "龙市", "涟水", "溧水", "娄山关南", "岚山西", "乐同", "龙塘镇", "灵武北", "洛湾三江", "朗县", "岚县", "泸县", "莱西", "郎溪南", "溧阳", "临沂北", "临沂北", "临邑", "柳园南", "龙游南", "鹿寨北", "临淄北", "林芝", "泸州", "阆中", "临泽南", "六枝南", "马鞍山东", "毛陈", "帽儿山西", "明港东", "民和南", "闵集", "马兰", "米兰", "弥勒", "民乐", "米林", "玛纳斯", "牟平", "民权北", "闽清北", "眉山东", "名山", "蒙山", "庙山", "岷县", "米易东", "门源", "暮云", "密云", "蒙自北", "孟庄", "蒙自", "梅州西", "南部", "南曹", "南充北", "牛车河", "南城", "宁城", "南昌", "南昌西", "宁东南", "宁东", "南芬北", "南丰", "南湖东", "牛河梁", "南华", "内江北", "内江东", "南江", "南江口", "奈林皋", "南陵", "牛栏山", "尼木", "南宁东", "南宁西", "南堡北", "南平市", "宁强南", "南通西", "宁县", "泥溪", "南雄", "南阳东", "纳雍", "南阳寨", "磐安南", "普安", "普安县", "屏边", "平坝南", "平昌", "普定", "平度", "平顶山西", "平度西", "蒲江", "皮口", "盘龙城", "蓬莱市", "屏南", "普宁", "平南南", "平泉北", "彭山北", "蒲石", "屏山", "坪上", "盘山", "平潭", "萍乡北", "濮阳", "鄱阳", "平遥古城", "平原东", "邳州东", "普者黑", "盘州", "平庄", "彭州", "攀枝花南", "彭州南", "秦安", "青白江东", "庆城", "清城", "青川", "青岛北", "青岛北", "青岛机场", "千岛湖", "启东", "祁东", "青堆", "青岛西", "前锋", "清河门北", "齐河", "曲靖北", "綦江东", "曲江", "邛崃", "青莲", "齐齐哈尔南", "清水北", "青神", "岐山", "庆盛", "清水县", "曲水县", "犍为", "祁县东", "黔西", "乾县", "旗下营南", "庆阳", "祁阳", "庆元", "青州市北", "乔庄东", "曲子", "全州南", "棋子湾", "清镇西", "仁布", "荣昌北", "荣成", "瑞昌西", "如东", "如皋南", "榕江", "日喀则", "饶平", "若羌", "日照西", "肃北", "舒城东", "遂昌", "宋城路", "三道湖", "邵东", "三都县", "胜芳", "双峰北", "商河", "泗洪", "双河市", "四会", "畲江北", "石家庄东", "三江南", "沙井西", "三井子", "四棵树", "双流机场", "双龙湖", "狮岭", "石林西", "双流西", "胜利镇", "三明北", "三明", "嵩明", "树木岭", "神木", "山南", "睢宁", "苏尼特左旗", "石牌", "山坡东", "沈丘北", "商丘东", "宿迁", "石桥", "沈丘", "桑日", "鄯善北", "狮山北", "三水北", "松山湖北", "狮山", "三水南", "韶山南", "泗水南", "三穗", "石梯", "汕尾", "绍兴北", "歙县北", "绍兴东", "松溪", "寿县", "泗县", "始兴", "随县", "泗阳", "松阳", "双洋", "三阳", "射阳", "双阳", "邵阳北", "松原北", "十堰东", "山阴", "邵阳西", "顺义西", "沈阳西", "深圳机场北", "深圳北", "深圳机场", "神州", "桑植", "十字门", "随州南", "尚志南", "石嘴山", "深圳坪山", "石柱县", "台安南", "太仓", "桃村北", "桐城东", "铁厂沟", "塔城", "桐城南", "太仓南", "铁刹山", "田东北", "土地堂东", "太谷东", "铁干里克", "太谷西", "吐哈", "通海", "太和北", "天河机场", "太和东", "天河街", "唐海南", "通化县", "同江", "托克托东", "铜陵北", "吐鲁番北", "桐庐", "头门港", "图木舒克", "泰宁", "铜仁南", "甜水堡", "天水南", "通渭", "田心东", "汤逊湖", "藤县", "太原南", "桃源", "通远堡西", "桐梓北", "太子城", "桐梓东", "通州", "万安县", "吴川", "文登东", "武当山西", "潍坊北", "五府山", "威虎岭北", "芜湖北", "威海北", "芜湖南", "苇河西", "温江", "魏家泉", "乌兰察布", "五龙背东", "乌龙泉南", "乌兰木图", "望牛墩", "五女山", "渭南西", "巍山", "武胜", "乌审旗", "乌苏", "五通", "无为", "无为南", "瓦屋山", "威信", "武乡", "闻喜西", "武夷山北", "武义北", "婺源", "渭源", "湾仔", "万州北", "湾仔北", "吴忠", "武陟", "梧州南", "湾沚南", "兴安北", "雄安", "许昌北", "许昌东", "项城", "新都东", "西渡", "咸丰", "西丰", "先锋", "湘府路", "襄汾西", "孝感北", "新干东", "孝感东", "兴国西", "夏格庄", "兴和北", "宣化北", "下花园北", "西湖东", "新化南", "西华", "新晃西", "新津", "小金口", "仙居南", "新津南", "辛集南", "西来", "兴隆县西", "新民北", "厦门", "咸宁东", "咸宁南", "溆浦南", "西平西", "响水县", "仙桃", "湘潭北", "邢台东", "新塘南", "兴文", "西乌旗", "修武西", "修文县", "萧县北", "新香坊北", "新乡东", "萧县", "岫岩", "新余北", "咸阳北", "西阳村", "信阳东", "襄阳东", "襄垣东", "秀英", "新沂南", "仙游", "祥云", "咸阳西", "新郑机场", "香樟路", "忻州西", "雅安", "永安南", "盐边", "迎宾路", "宜宾西", "亚布力西", "永城北", "盐城北", "运城北", "永川东", "禹城东", "盐城大丰", "宜春", "岳池", "云东海", "姚渡", "云浮东", "永福南", "雨格", "阳高南", "洋河", "永济北", "延吉西", "英库勒", "永康南", "依兰", "运粮河", "尉犁", "炎陵", "鄢陵", "杨陵南", "羊马", "一面坡北", "云梦东", "伊敏", "元谋西", "郁南", "云南驿", "延平东", "延平", "银瓶", "延平西", "原平西", "阳泉东", "雁栖湖", "杨桥", "阳曲西", "永仁", "颍上北", "永寿", "阳朔", "云山", "玉山南", "雁石南", "榆社西", "永寿西", "银滩", "永泰", "鹰潭北", "依吞布拉克", "烟台南", "伊通", "烟台西", "云霄", "云县", "玉溪", "阳信", "尤溪", "宜兴", "应县", "攸县南", "洋县西", "义县西", "余姚北", "扬州东", "银盏", "禹州", "榆中", "诏安", "淄博北", "正定机场", "准东", "纸坊东", "柘皋", "准格尔", "自贡", "庄河北", "政和", "珠海长隆", "昭化", "织金北", "张家川", "张家港", "织金", "芷江", "张家口南", "张家界西", "仲恺", "周口东", "曾口", "珠琳", "左岭", "樟木头东", "驻马店西", "扎囊", "周宁", "邹平", "漳浦", "漳平西", "章丘北", "肇庆东", "庄桥", "昭山", "钟山西", "朱砂古镇", "中堂", "支提山", "中卫南", "漳县", "镇雄", "资阳北", "遵义", "遵义南", "张掖西", "资中北", "卓资东", "枣庄东", "涿州东", "郑州东", "株洲南");
        Set<String> list = new HashSet<>();
        for (String string : strings) {
            if (line.contains(string)) list.add(string);
        }
        return list;
    }


    // 获取买火车票的来源
    public static String matchingSource(String line) {
        String msgOriginName = "";
        Pattern p5 = Pattern.compile("【.*?】"); // 【中山农商银行】
        Pattern p6 = Pattern.compile("\\[.*?\\]");
        Matcher m5 = p5.matcher(line.trim());
        Matcher m6 = p6.matcher(line.trim());
        ArrayList<String> list = new ArrayList<>();
        while (m5.find()) list.add(m5.group());
        while (m6.find()) list.add(m6.group());

        msgOriginName = list.get(0);
        return msgOriginName.replace("【", "").replace("】", "").replace("[", "").replace("]", "");

    }

    // 验证关键字前面是否是汉字
    public static boolean VerifyChinese(String key, String line) {
        int i = line.indexOf(key);
        // 前面有中文
        if (i - 1 > 0) {
            char c = line.charAt(i - 1);
            System.out.println(key + "," + c);
            return isChineseCharacter(c);
        }
        return false;
    }

    // 提取火车票
    public static String validation(String line) {
        // 火车票开头
        String[] str = {"C", "D", "G", "Z", "T", "K", "L", "c", "d", "g", "z", "t", "k", "l"};
        String regular = "\\d{2,4}";
        // 飞机编号提取
        if (line.contains("起飞")) {
            regular = "\\d{1,4}";

            for (String s : str) {
                Pattern p1 = Pattern.compile(s + regular);
                Matcher m1 = p1.matcher(line.trim());
                while (m1.find()) {
                    String group = m1.group();
                    // 如果班次前面有中文就返回
                    if (VerifyChinese(group, line)) {
                        return group;
                    }

                }
            }
        }
        for (String s : str) {
            Pattern p1 = Pattern.compile(s + regular);
            Matcher m1 = p1.matcher(line.trim());
            if (m1.find()) return m1.group();
        }

        return "";
    }

    // 验证字符是否是汉字
    public static boolean isChineseCharacter(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }
}