package com.reptile.Bank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.reptile.util.CloseDriver;
import com.reptile.util.PushSocket;
import com.reptile.util.PushState;
import com.reptile.util.Resttemplate;
import com.reptile.util.SpecialStr;
import com.reptile.util.application;
import com.reptile.util.KeysPress;
import com.reptile.winio.VKMapping;
@SuppressWarnings("deprecation")

/**
 * @author Bigyoung
 * @version v1.0
 * @deprecated 农业银行
 * @date 2017年11月16日10:51:14
 */
public class AbcBank {
	public   static Map<String,Object> NongYe(String username,String userpwd,String UUID,String card) throws Exception {
		{
			Map<String, Object>	params	= new HashMap<String, Object>();
			Map<String, String>	headers = new HashMap<String, String>();
			Map<String, Object>	status	= new HashMap<String, Object>();
			Map<String, Object>	maps	= new HashMap<String, Object>();
		/*打开此网页*/
		WebDriver driver= KeysPress.OpenUrl( "ie", "https://perbank.abchina.com/EbankSite/startup.do", "C:/ie/IEDriverServer.exe" );
		/*判断是否加载页面*/
		CloseDriver.DriverClose(driver.getTitle(), driver,5);
		/*键入账号*/
		WebElement element= driver.findElement(By.id("username"));
		element.sendKeys(username);
		KeysPress.SendTab( "Tab" );
		/* 输入密码 */
		//		keysPress.SenStr(userPwd);
		/*特殊字符处理*/
		SpecialStr.SpecialStr(userpwd);
		/* 输入验证码 */
		WebElement	elements	= driver.findElement( By.id( "vCode" ) );
		WebElement	code	= driver.findElement( By.id( "code" ) );
		String		imgtext		= BcmLogin.downloadImgs(driver, elements,10,10);
		code.sendKeys( imgtext );
		/* 登陆*/
		WebElement	logo	= driver.findElement( By.id( "logo" ) );
		logo.click();
		Thread.sleep(5000);
		System.out.println(driver.getTitle());
		if(driver.getTitle().equals("中国农业银行个人网银首页")){
			/* 登陆成功*/
			PushState.state(card, "bankBillFlow",100);
			PushSocket.push(status, UUID, "0000");//开始执行推送登陆成功
			
			driver.switchTo().frame("contentFrame");
			/*拿到姓名*/
			WebElement	custName	= driver.findElement( By.id("show-custName"));
			String cusname=custName.getText();

			WebElement	dh	= driver.findElement( By.linkText("本行账户"));
			dh.click();
			/*选择账号明细*/
			WebElement	mx	= driver.findElement( By.linkText("明细"));
			//隐患：万一多张卡 拿到的就是多个明细
			mx.click();
			/*选择日期*/
			WebElement	startDate	= driver.findElement( By.id("startDate"));
			List<Map<String,Object>> data=BcmLogin.yuefen();
			//隐患：万一多张卡 拿到的就是多个明细
			startDate.clear();
			startDate.sendKeys( data.get( 0 ).get( "begin" ).toString());
			WebElement	btn_query	= driver.findElement( By.id("btn_query"));
			Thread.sleep(2000);
			btn_query.click();
			Thread.sleep(2000);
			 /*拿到开户行 以及卡号*/
			WebElement	label	= driver.findElement( By.className("label"));
			 /*卡号*/
			String[] sp=label.toString().split("|");

			
			/*开始解析*/
			Document	docs	= Jsoup.parse(driver.getPageSource());
			Element	trs	= docs.getElementById("AccountTradeDetailTable");
			Elements	tr	= trs.select( "tr" );
			List<Object>	list	= new ArrayList<Object>();
			for ( int i = 0; i < tr.size(); i++ )	{
				Map<String, Object>map = new HashMap<String, Object>();
				Elements td = tr.get( i ).select( "td" );
				for ( int j = 0; j < td.size(); j++ ){
					if(j==0){
					/*交易时间*/	
						map.put("dealTime",  td.get( j ).text());
				
					}
					if(j==1){
						/*交易金额*/	
			
						if(td.get( j ).text().contains("+")){
							map.put("incomeMoney",  td.get( j ).text());
							
						}else{
							map.put("expendMoney",  td.get( j ).text());
						}
					}
					if(j==2){
						/*本次余额*/	
						map.put("balanceAmount",  td.get( j ).text());
					}
					if(j==3){
						/*对方信息 姓名加卡号*/	
					}
					if(j==4){
						/*交易类型*/	
						
					}
					if(j==5){
						/*交易渠道*/	
						map.put("dealDitch",  td.get( j ).text());
						
					}
					if(j==6){
						/*交易摘要*/	
						map.put("dealReferral",  td.get( j ).text());
						
					}
					map.put( "oppositeSideName", "" );
					map.put( "oppositeSideNumber", "" );
					map.put( "currency", "" );
					list.add( map );
				}
			}
			params.clear();
			headers.clear();
			headers.put( "accountType", "" );/*账号状态*/
			headers.put( "openBranch", sp[2] );/*开户网点*/
			headers.put( "openTime", "" );/*开户日期*/

			params.put( "billMes", list );
			params.put( "baseMes", headers );
			params.put( "IDNumber","");/*身份证*/
			params.put( "cardNumber", sp[1]);/*用户卡号*/
			params.put( "userName", cusname);/*用户姓名*/
			Resttemplate resttemplate = new Resttemplate();
			status = resttemplate.SendMessage( JSONObject.fromObject( params ),application.sendip+"/HSDC/savings/authentication",card);
			driver.quit();
			return status;
		} else{
			PushState.state(card, "bankBillFlow",200);
			System.out.println("账号密码有无");
			driver.quit();
			status.put("errorCode","0001");//异常处理
			status.put("errorInfo","密码错误");
			return status;
		}
	}
}
}