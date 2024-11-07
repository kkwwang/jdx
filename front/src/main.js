import router from "./router";
import {
    ActionSheet,
    Button,
    Calendar,
    Card,
    Cell,
    CellGroup,
    CountDown,
    Dialog,
    Divider,
    Empty,
    Field,
    Form,
    Grid,
    GridItem,
    Icon,
    Image as VanImage,
    ImagePreview,
    List,
    Loading,
    NavBar,
    NoticeBar,
    Picker,
    Popover,
    Popup,
    Radio,
    RadioGroup,
    Step,
    Steps,
    SwipeCell,
    Tab,
    Tabbar,
    TabbarItem,
    Tabs,
    Tag,
    Toast,
    Switch
} from "vant";
import VueClipboard from "vue-clipboard2";
import {createApp} from "vue";
import App from "./App.vue";
import "vant/lib/index.css"

const app = createApp(App);


app.config.productionTip = false;

app.use(Icon);
app.use(Button);
app.use(NavBar);
app.use(Form);
app.use(Field);
app.use(VanImage);
app.use(ImagePreview);
app.use(Tabbar);
app.use(TabbarItem);
app.use(Grid);
app.use(GridItem);
app.use(Divider);
app.use(NoticeBar);
app.use(Tab);
app.use(Tabs);
app.use(Cell);
app.use(CellGroup);
app.use(SwipeCell);
app.use(Card);
app.use(ActionSheet);
app.use(Empty);
app.use(Popover);
app.use(Picker);
app.use(Popup);
app.use(CountDown);
app.use(Radio);
app.use(RadioGroup);
app.use(Loading);
app.use(Step);
app.use(Steps);
app.use(List);
app.use(Tag);
app.use(Dialog);
app.use(Calendar);
app.use(    Switch
);

app.use(VueClipboard);
app.use(router);

app.config.globalProperties.$toast = Toast;
app.config.globalProperties.$dialog = Dialog;

app.mount("#app");
