package com.codefarm.fastdfs.monitor.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.codefarm.fastdfs.monitor.service.MonitorService;
import com.codefarm.fastdfs.monitor.service.WarningService;
import com.codefarm.fastdfs.monitor.vo.Forecast;
import com.codefarm.fastdfs.monitor.vo.Group;
import com.codefarm.fastdfs.monitor.vo.Line;
import com.codefarm.fastdfs.monitor.vo.Storage;
import com.codefarm.fastdfs.monitor.vo.WarningData;

@Controller
@RequestMapping("/forecast")
public class ForecastAction
{
    @Autowired
    private MonitorService monitorService;
    
    @Autowired
    private WarningService warningService;
    
    @RequestMapping("/dilatation")
    public ModelAndView dilatation() throws Exception
    {
        ModelAndView mv = new ModelAndView("forecast/dilatation.jsp");
        List<Group> groupInfo = monitorService.listGroupInfo();
        mv.addObject("groupInfo", groupInfo);
        
        return mv;
    }
    
    @ResponseBody
    @RequestMapping("/getDilatation")
    public List<Line> getDilatation(String ip) throws Exception
    {
        List<Line> lineList = new ArrayList<Line>();
        Line lines = new Line(ip);
        Forecast forecast = getForecastObject(ip);
        if (forecast.getIpAddr() != null)
        {
            long average = forecast.getAverage();
            Calendar timeForForecast = Calendar.getInstance();
            timeForForecast.setTime(forecast.getTimeForForecast());
            lines.getData().add(new Long[] { timeForForecast.getTimeInMillis(),
                    forecast.getWarning() / 1024 });
            for (int i = 0; i < 12; i++)
            {
                long freeMB = (forecast.getWarning() + average * (i + 1) * 24
                        * 30) / 1024;
                timeForForecast.add(Calendar.MONTH, 1); // 加一个月
                // timeForForecast.set(Calendar.DATE, 1);     // 设置当前月第一天
                lines.getData().add(new Long[] {
                        timeForForecast.getTimeInMillis(), freeMB });
                
            }
        }
        lineList.add(lines);
        return lineList;
    }
    
    @RequestMapping("/bottleneck")
    public ModelAndView bottleneck() throws Exception
    {
        ModelAndView mv = new ModelAndView("forecast/bottleneck.jsp");
        List<Group> groups = monitorService.listGroupInfo();
        mv.addObject("groups", groups);
        return mv;
    }
    
    @ResponseBody
    @RequestMapping("/drawAreaAction")
    public List<Line> drawAreaAction(String ip) throws Exception
    {
        List<Line> lines = new ArrayList<Line>();
        Forecast forecast = getForecastObject(ip);
        Line line = new Line(ip);
        if (forecast.getIpAddr() != null)
        {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int size = (int) (forecast.getUseHour() % 24 == 0 ? forecast.getUseHour() / 24
                    : forecast.getUseHour() / 24 + 1);
            for (int i = 0; i < size; i++)
            {
                c.add(Calendar.DAY_OF_MONTH, 1);
                Date date = c.getTime();
                long useMB = (forecast.getFreeMB() - 24 * forecast.getAverage()
                        * (i)) / 1024;
                line.getData().add(new Object[] { date, useMB });
            }
        }
        lines.add(line);
        return lines;
    }
    
    public Forecast getForecastObject(String ip) throws Exception
    {
        Forecast forecast = new Forecast();
        List<Group> groupList = monitorService.listGroupInfo();
        for (Group group : groupList)
        {
            for (Storage storage : group.getStorageList())
            {
                if (storage.getIpAddr().equals(ip)
                        && storage.getCurStatus().equals("ACTIVE"))
                {
                    long d1 = new Date().getTime();
                    long d2 = storage.getJoinTime().getTime();
                    long day = (d1 - d2) / (24 * 60 * 60 * 1000);
                    long hour = (d1 - d2) / (60 * 60 * 1000);
                    long hasUse = storage.getTotalMB() - storage.getFreeMB();
                    long average = (long) hasUse / hour;
                    forecast.setAverage(average);
                    forecast.setIpAddr(storage.getIpAddr());
                    
                    List<WarningData> warningData = new ArrayList<WarningData>();
                    warningData = warningService.findByIp(storage.getIpAddr());
                    long wdFreeMB = 0;
                    if (!warningData.isEmpty())
                    {
                        wdFreeMB = warningData.get(0).getWdFreeMB();
                    }
                    forecast.setWarning(wdFreeMB * 1024);
                    //下面计算预测到预警时间
                    long mayUse = storage.getFreeMB() - wdFreeMB * 1024;//可以用的容量
                    long forecastHour = (long) mayUse / average;
                    long d3 = d1 + forecastHour * 60 * 60 * 1000;
                    Date forecastTime = new Date(d3);
                    forecast.setTimeForForecast(forecastTime);
                    forecast.setFreeMB(storage.getFreeMB());
                    forecast.setUseHour(forecastHour);
                    forecast.setNow(new Date());
                    
                }
            }
        }
        
        return forecast;
    }
    
}
