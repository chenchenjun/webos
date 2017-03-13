package spc.webos.web.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import spc.webos.config.AppConfig;
import spc.webos.constant.Config;
import spc.webos.web.common.SUI;

public class VerifyImageServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		doRequest(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		doRequest(request, response);
	}

	protected void doRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{

		response.setContentType("image/gif");
		response.setHeader("Cache-Control", "no-cache");
		int width = 60, height = 20,
				num = AppConfig.getInstance().getProperty(Config.app_login_verify_len, false, 4);
		String strWidth = (String) request.getParameter("w");
		String strHeight = (String) request.getParameter("h");
		if (strWidth != null && strWidth.length() > 0) width = Integer.parseInt(strWidth);
		if (strHeight != null && strHeight.length() > 0) height = Integer.parseInt(strHeight);

		ServletOutputStream out = response.getOutputStream();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); // ����ͼƬ��С��
		Graphics gra = image.getGraphics();
		Random random = new Random();

		gra.setColor(getRandColor(200, 250)); // ���ñ���ɫ
		gra.fillRect(0, 0, width, height);

		gra.setColor(Color.black); // ��������ɫ
		gra.setFont(mFont);

		/*
		 * gra.setColor(new Color(0)); gra.drawRect(0,0,width-1,height-1);
		 */

		// �������155�������ߣ�ʹͼ���е���֤�벻�ױ���������̽�⵽
		gra.setColor(getRandColor(160, 200));
		for (int i = 0; i < 155; i++)
		{
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int xl = random.nextInt(12);
			int yl = random.nextInt(12);
			gra.drawLine(x, y, x + xl, y + yl);
		}

		// ȡ�����������֤��(4λ����)
		String sRand = "";
		for (int i = 0; i < num; i++)
		{
			String rand = String.valueOf(random.nextInt(10));
			sRand += rand;
			// ����֤����ʾ��ͼ����
			gra.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110),
					20 + random.nextInt(110)));// ���ú�����������ɫ��ͬ����������Ϊ����̫�ӽ�������ֻ��ֱ������
			gra.drawString(rand, 13 * i + 6, 16);
		}
		SUI sui = SUI.SUI.get();
		if (sui != null)
		{
			sui.setVerifyCode(sRand);
			log.info("verify image:{}", sRand);
		}
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		encoder.encode(image);
		out.close();
	}

	static Color getRandColor(int fc, int bc)
	{// ������Χ��������ɫ
		Random random = new Random();
		if (fc > 255) fc = 255;
		if (bc > 255) bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

	Font mFont = new Font("Times New Roman", Font.PLAIN, 20);// ��������
	protected Logger log = LoggerFactory.getLogger(getClass());
}
