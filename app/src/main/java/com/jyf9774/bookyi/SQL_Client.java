package com.jyf9774.bookyi;

import java.lang.reflect.AccessibleObject;
import java.sql.*;
import java.util.ArrayList;

public class SQL_Client {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static String DB_URL = "jdbc:mysql:// ;
    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = "";

    public String login(String Account, String passwd) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT *FROM user";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String get_id = rs.getString("user_name");
                String get_Passwd = rs.getString("password");

                if (get_id.equals(Account)) {
                    if (get_Passwd.equals(passwd)) {
                        //System.out.println("登录成功");
                        return "登录成功";

                    } else {
                        //System.out.println("密码错误");
                        return "密码错误";
                    }
                }
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
            return "账号不存在";

        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            return "JDBC错误代码" + se.getErrorCode();

        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            return "Class.forName 错误代码" + e.getMessage();

        }
    }

    public String check(String Account) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT *FROM user";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String get_id = rs.getString("user_name");

                if (get_id.equals(Account)) {
                    return "用户名被占用";
                }
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
            return "用户名可用";

        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            return "JDBC错误代码" + se.getErrorCode();

        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            return "Class.forName 错误代码" + e.getMessage();

        }
    }

    public User getUserByName(String username) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM user WHERE user_name ='" + username + "'";
            ResultSet rs = stmt.executeQuery(sql);
            User user = new User();
            while (rs.next()) {

                user.username = rs.getString("user_name");
                user.password = rs.getString("password");
                user.phoneNumber = rs.getString("phone_number");
                user.QQNumber = rs.getString("qq_number");
                user.weChatNumber = rs.getString("wechat_number");
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
            return user;

        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            System.out.println("JDBC错误代码" + se.getErrorCode());
            User wrongUser = new User();
            wrongUser.username = "JDBC错误代码" + se.getErrorCode();
            return wrongUser;
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            System.out.println("Class.forName 错误代码" + e.getMessage());
            User wrongUser = new User();
            wrongUser.username = "Class.forName 错误代码" + e.getMessage();
            return wrongUser;

        }
    }

    public String updateUser(String username, String phoneNumber, String qqNumber, String weChatNumber) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "UPDATE `mybook`.`user` SET `phone_number`='" + phoneNumber + "', `qq_number`='" + qqNumber + "', `wechat_number`='" + weChatNumber + "' WHERE  `user_name`='" + username + "'";
            stmt.execute(sql);
            // 完成后关闭
            stmt.close();
            conn.close();
            return "联系方式更新成功";

        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            return "JDBC错误代码" + se.getErrorCode();

        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            return "Class.forName 错误代码" + e.getMessage();

        }

    }

    public String updateUserPassword(String username, String oldPassword, String newPassword) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT password FROM user WHERE  `user_name`='" + username + "'";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String temp = rs.getString("password");
                if (oldPassword.equals(temp)) {
                    break;
                } else {
                    return "旧密码错误，请检查您的输入";
                }
            }
            sql = "UPDATE `mybook`.`user` SET `password`='" + newPassword + "' WHERE  `user_name`='" + username + "'";
            stmt.execute(sql);
            // 完成后关闭
            stmt.close();
            conn.close();
            return "密码更新成功";

        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            return "JDBC错误代码" + se.getErrorCode();

        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            return "Class.forName 错误代码" + e.getMessage();

        }

    }

    public String regist(String Account, String passwd, String phone_number, String qq_number, String wechat_number) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "INSERT INTO `mybook`.`user` (`user_name`,`password`,`phone_number`,`qq_number`,`wechat_number`) VALUES ('" + Account + "', '" + passwd + "', '" + phone_number + "', '" + qq_number + "', '" + wechat_number + "');";
            stmt.execute(sql);

            stmt.close();
            conn.close();

            return "注册成功";


        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            return "JDBC错误代码" + se.getErrorCode();

        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            return "Class.forName 错误代码" + e.getMessage();

        }
    }

    public String uploadBook(String bookId, String userName, String bookName, String bookStatement,
                             String bookPicture, boolean bookSaleOrBorrow, String bookPrice, String bookBorrowDate) {
        if (bookSaleOrBorrow) {
            System.out.println("调用后：出售");
        } else {
            System.out.println("调用后：出借");
        }
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            if (bookSaleOrBorrow) {
                sql = "INSERT INTO `mybook`.`book` (`book_id`, `user_name`, `book_name`, `book_statement`, `book_picture`, `book_price`) " +
                        "VALUES ('" + bookId + "', '" + userName + "', '" + bookName + "', '" + bookStatement + "', '" + bookPicture + "', '" + bookPrice + "')";

            } else {
                sql = "INSERT INTO `mybook`.`book` (`book_id`, `user_name`, `book_name`, `book_statement`, `book_picture`, `book_sale_or_borrow`, `book_borrow_date`) " +
                        "VALUES ('" + bookId + "', '" + userName + "', '" + bookName + "', '" + bookStatement + "', '" + bookPicture + "', '1', '" + bookBorrowDate + "');";
            }
            stmt.execute(sql);

            stmt.close();
            conn.close();

            return "书籍信息上传成功";


        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            return "JDBC错误代码" + se.getErrorCode();

        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            return "Class.forName 错误代码" + e.getMessage();

        }
    }

    public ArrayList<Book> getAllBook() {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM book";
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<Book> books = new ArrayList();
            while (rs.next()) {
                Book book = new Book();
                book.bookId = rs.getString("book_id");
                book.username = rs.getString("user_name");
                book.bookName = rs.getString("book_name");
                book.bookStatement = rs.getString("book_statement");
                book.bookPicture = rs.getString("book_picture");
                book.bookSaleOrBorrow = rs.getBoolean("book_sale_or_borrow");
                if (!book.bookSaleOrBorrow) {
                    book.bookPrice = rs.getString("book_price");
                } else {
                    book.bookBorrowDate = rs.getString("book_borrow_date");
                }
                book.bookSaled = rs.getBoolean("book_saled");
                book.uploadTime = rs.getString("upload_time");
                if (book.bookSaled) {
                    //过滤已出售的书本
                } else {
                    books.add(book);
                }
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
            return books;

        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            System.out.println("JDBC错误代码" + se.getErrorCode());
            ArrayList<Book> wrong = new ArrayList<>();
            Book wrongBook = new Book();
            wrongBook.bookName = "JDBC错误代码" + se.getErrorCode();
            wrong.add(wrongBook);
            return wrong;
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            System.out.println("Class.forName 错误代码" + e.getMessage());
            ArrayList<Book> wrong = new ArrayList<>();
            Book wrongBook = new Book();
            wrongBook.bookName = "Class.forName 错误代码" + e.getMessage();
            wrong.add(wrongBook);
            return wrong;

        }
    }//MainActivity

    public ArrayList<Book> searchBook(String keyWords) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM book WHERE book_name LIKE'%" + keyWords + "%'";
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<Book> books = new ArrayList();
            while (rs.next()) {
                Book book = new Book();
                book.bookId = rs.getString("book_id");
                book.username = rs.getString("user_name");
                book.bookName = rs.getString("book_name");
                book.bookStatement = rs.getString("book_statement");
                book.bookPicture = rs.getString("book_picture");
                book.bookSaleOrBorrow = rs.getBoolean("book_sale_or_borrow");
                if (!book.bookSaleOrBorrow) {
                    book.bookPrice = rs.getString("book_price");
                } else {
                    book.bookBorrowDate = rs.getString("book_borrow_date");
                }
                book.bookSaled = rs.getBoolean("book_saled");
                book.uploadTime = rs.getString("upload_time");
                books.add(book);
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
            return books;

        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            System.out.println("JDBC错误代码" + se.getErrorCode());
            ArrayList<Book> wrong = new ArrayList<>();
            Book wrongBook = new Book();
            wrongBook.bookName = "JDBC错误代码" + se.getErrorCode();
            wrong.add(wrongBook);
            return wrong;
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            System.out.println("Class.forName 错误代码" + e.getMessage());
            ArrayList<Book> wrong = new ArrayList<>();
            Book wrongBook = new Book();
            wrongBook.bookName = "Class.forName 错误代码" + e.getMessage();
            wrong.add(wrongBook);
            return wrong;

        }
    }//MainActivity

    public ArrayList<Book> findBookByUser(String username) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM book WHERE user_name ='" + username + "'";
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<Book> books = new ArrayList();
            while (rs.next()) {
                Book book = new Book();
                book.bookId = rs.getString("book_id");
                book.username = rs.getString("user_name");
                book.bookName = rs.getString("book_name");
                book.bookStatement = rs.getString("book_statement");
                book.bookPicture = rs.getString("book_picture");
                book.bookSaleOrBorrow = rs.getBoolean("book_sale_or_borrow");
                if (!book.bookSaleOrBorrow) {
                    book.bookPrice = rs.getString("book_price");
                } else {
                    book.bookBorrowDate = rs.getString("book_borrow_date");
                }
                book.bookSaled = rs.getBoolean("book_saled");
                book.uploadTime = rs.getString("upload_time");
                books.add(book);
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
            return books;

        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            System.out.println("JDBC错误代码" + se.getErrorCode());
            ArrayList<Book> wrong = new ArrayList<>();
            Book wrongBook = new Book();
            wrongBook.bookName = "JDBC错误代码" + se.getErrorCode();
            wrong.add(wrongBook);
            return wrong;
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            System.out.println("Class.forName 错误代码" + e.getMessage());
            ArrayList<Book> wrong = new ArrayList<>();
            Book wrongBook = new Book();
            wrongBook.bookName = "Class.forName 错误代码" + e.getMessage();
            wrong.add(wrongBook);
            return wrong;

        }
    }//MyBookActivity

    public ArrayList<Book> searchUserBook(String username, String keyWords) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM book WHERE user_name = '" + username + "' AND book_name LIKE'%" + keyWords + "%'";
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<Book> books = new ArrayList();
            while (rs.next()) {
                Book book = new Book();
                book.bookId = rs.getString("book_id");
                book.username = rs.getString("user_name");
                book.bookName = rs.getString("book_name");
                book.bookStatement = rs.getString("book_statement");
                book.bookPicture = rs.getString("book_picture");
                book.bookSaleOrBorrow = rs.getBoolean("book_sale_or_borrow");
                if (!book.bookSaleOrBorrow) {
                    book.bookPrice = rs.getString("book_price");
                } else {
                    book.bookBorrowDate = rs.getString("book_borrow_date");
                }
                book.bookSaled = rs.getBoolean("book_saled");
                book.uploadTime = rs.getString("upload_time");
                books.add(book);
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
            return books;

        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            System.out.println("JDBC错误代码" + se.getErrorCode());
            ArrayList<Book> wrong = new ArrayList<>();
            Book wrongBook = new Book();
            wrongBook.bookName = "JDBC错误代码" + se.getErrorCode();
            wrong.add(wrongBook);
            return wrong;
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            System.out.println("Class.forName 错误代码" + e.getMessage());
            ArrayList<Book> wrong = new ArrayList<>();
            Book wrongBook = new Book();
            wrongBook.bookName = "Class.forName 错误代码" + e.getMessage();
            wrong.add(wrongBook);
            return wrong;

        }
    }//MyBookActivity

    public Book findBook(String bookId) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM book WHERE book_id ='" + bookId + "'";
            ResultSet rs = stmt.executeQuery(sql);
            Book book = new Book();
            while (rs.next()) {
                book.bookId = rs.getString("book_id");
                book.username = rs.getString("user_name");
                book.bookName = rs.getString("book_name");
                book.bookStatement = rs.getString("book_statement");
                book.bookPicture = rs.getString("book_picture");
                book.bookSaleOrBorrow = rs.getBoolean("book_sale_or_borrow");
                if (!book.bookSaleOrBorrow) {
                    book.bookPrice = rs.getString("book_price");
                } else {
                    book.bookBorrowDate = rs.getString("book_borrow_date");
                }
                book.bookSaled = rs.getBoolean("book_saled");
                book.uploadTime = rs.getString("upload_time");
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
            return book;

        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            System.out.println("JDBC错误代码" + se.getErrorCode());
            Book wrongBook = new Book();
            wrongBook.bookName = "JDBC错误代码" + se.getErrorCode();
            return wrongBook;
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            System.out.println("Class.forName 错误代码" + e.getMessage());
            Book wrongBook = new Book();
            wrongBook.bookName = "Class.forName 错误代码" + e.getMessage();
            return wrongBook;

        }
    }

    public String deleteBookByID(String bookId) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "DELETE FROM book WHERE book_id ='" + bookId + "'";
            stmt.execute(sql);

            stmt.close();
            conn.close();

            return "删除成功";

        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            return "JDBC错误代码" + se.getErrorCode();

        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            return "Class.forName 错误代码" + e.getMessage();

        }
    }

    public String createOrder(String orderId, String buyerName, String ownerName,
                              String bookId, String bookName, String requestStatement) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM book WHERE book_id ='" + bookId + "'";
            ResultSet rs = stmt.executeQuery(sql);
            boolean isSaled = false;
            int count = 0;
            while (rs.next()) {
                count++;
                isSaled = rs.getBoolean("book_saled");
                if (isSaled) {
                    return "书籍已售出";
                }

            }
            if (count == 0) {
                return "书籍不存在";
            }
            sql = "INSERT INTO `mybook`.`book_order` (`order_id`,`buyer_name`,`owner_name`,`book_id`,`book_name`,`request_statement`) VALUES ('" + orderId + "', '" + buyerName + "', '" + ownerName + "', '" + bookId + "', '" + bookName + "', '" + requestStatement + "');";
            stmt.execute(sql);

            stmt.close();
            conn.close();

            return "订单创建成功";


        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            return "JDBC错误代码" + se.getErrorCode();

        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            return "Class.forName 错误代码" + e.getMessage();

        }
    }

    public ArrayList<Order> getBuyerOrder(String username) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM book_order WHERE buyer_name ='" + username + "'";
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<Order> orders = new ArrayList();
            while (rs.next()) {
                Order order = new Order();
                order.orderId = rs.getString("order_id");
                order.bookId = rs.getString("book_id");
                order.bookName = rs.getString("book_name");
                order.buyerName = rs.getString("buyer_name");
                order.ownerName = rs.getString("owner_name");
                order.requestStatement = rs.getString("request_statement");
                order.confirmStatement = rs.getString("confirm_statement");
                if (rs.getString("order_confirmed").equals("0")) {
                    order.state = "待确认";
                } else if (rs.getString("order_confirmed").equals("1")) {
                    order.state = "已完成";
                } else {
                    order.state = "已关闭";
                }
                order.createTime = rs.getString("order_create_time");
                order.confirmTime = rs.getString("order_confirm_time");
                orders.add(order);
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
            return orders;

        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            System.out.println("JDBC错误代码" + se.getErrorCode());
            ArrayList<Order> wrong = new ArrayList<>();
            Order wrongOrder = new Order();
            wrongOrder.orderId = "JDBC错误代码" + se.getErrorCode();
            wrong.add(wrongOrder);
            return wrong;
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            System.out.println("Class.forName 错误代码" + e.getMessage());
            ArrayList<Order> wrong = new ArrayList<>();
            Order wrongOrder = new Order();
            wrongOrder.orderId = "Class.forName 错误代码" + e.getMessage();
            wrong.add(wrongOrder);
            return wrong;

        }
    }

    public ArrayList<Order> getOwnerOrder(String username) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM book_order WHERE owner_name ='" + username + "'";
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<Order> orders = new ArrayList();
            while (rs.next()) {
                Order order = new Order();
                order.orderId = rs.getString("order_id");
                order.bookId = rs.getString("book_id");
                order.bookName = rs.getString("book_name");
                order.buyerName = rs.getString("buyer_name");
                order.ownerName = rs.getString("owner_name");
                order.requestStatement = rs.getString("request_statement");
                order.confirmStatement = rs.getString("confirm_statement");
                if (rs.getString("order_confirmed").equals("0")) {
                    order.state = "待确认";
                } else if (rs.getString("order_confirmed").equals("1")) {
                    order.state = "已完成";
                } else {
                    order.state = "已关闭";
                }
                order.createTime = rs.getString("order_create_time");
                order.confirmTime = rs.getString("order_confirm_time");
                orders.add(order);
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
            return orders;

        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            System.out.println("JDBC错误代码" + se.getErrorCode());
            ArrayList<Order> wrong = new ArrayList<>();
            Order wrongOrder = new Order();
            wrongOrder.orderId = "JDBC错误代码" + se.getErrorCode();
            wrong.add(wrongOrder);
            return wrong;
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            System.out.println("Class.forName 错误代码" + e.getMessage());
            ArrayList<Order> wrong = new ArrayList<>();
            Order wrongOrder = new Order();
            wrongOrder.orderId = "Class.forName 错误代码" + e.getMessage();
            wrong.add(wrongOrder);
            return wrong;

        }
    }

    public Order findOrder(String orderId){
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM book_order WHERE order_id ='" + orderId + "'";
            ResultSet rs = stmt.executeQuery(sql);
            Order order = new Order();
            while (rs.next()) {
                order.orderId = rs.getString("order_id");
                order.buyerName = rs.getString("buyer_name");
                order.ownerName = rs.getString("owner_name");
                order.requestStatement = rs.getString("request_statement");
                order.confirmStatement = rs.getString("confirm_statement");
                order.bookId = rs.getString("book_id");
                order.bookName = rs.getString("book_name");
                if (rs.getString("order_confirmed").equals("0")) {
                    order.state = "待确认";
                } else if (rs.getString("order_confirmed").equals("1")) {
                    order.state = "已完成";
                } else {
                    order.state = "已关闭";
                }
                order.createTime = rs.getString("order_create_time");
                order.confirmTime = rs.getString("order_confirm_time");
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
            return order;

        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            System.out.println("JDBC错误代码" + se.getErrorCode());
            Order wrongOrder = new Order();
            wrongOrder.requestStatement = "JDBC错误代码" + se.getErrorCode();
            return wrongOrder;
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            System.out.println("Class.forName 错误代码" + e.getMessage());
            Order wrongOrder = new Order();
            wrongOrder.requestStatement = "Class.forName 错误代码" + e.getMessage();
            return wrongOrder;

        }
    }

    public String confirmOrder(String orderId,String bookId,String confirmStatement,boolean accept){
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            if(accept){
                sql = "UPDATE `mybook`.`book` SET `book_saled`='1' WHERE  `book_id`='" + bookId + "'";
                stmt.execute(sql);
                sql = "UPDATE `mybook`.`book_order` SET `order_confirmed`='2',`confirm_statement`='书主已与他人成交' " +
                        "WHERE  `order_id`!='" + orderId + "' AND book_id = '"+bookId+"'";
                stmt.execute(sql);
                sql = "UPDATE `mybook`.`book_order` SET `order_confirmed`='1'," +
                        "`confirm_statement`='"+confirmStatement+"' WHERE  `order_id`='" + orderId + "'";
                stmt.execute(sql);
                stmt.close();
                conn.close();
                return "成交已确认，您的联系方式将展示给对方";
            }else{
                sql = "UPDATE `mybook`.`book_order` SET `order_confirmed`='2',`" +
                        "confirm_statement`='"+confirmStatement+"' WHERE  `order_id`='" + orderId + "'";
                stmt.execute(sql);
                // 完成后关闭
                stmt.close();
                conn.close();
                return "交易已婉拒";
            }


        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            return "JDBC错误代码" + se.getErrorCode();

        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            return "Class.forName 错误代码" + e.getMessage();

        }

    }

    public String deleteOrderByID(String orderId) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 执行查询
            //System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "DELETE FROM book_order WHERE order_id ='" + orderId + "'";
            stmt.execute(sql);

            stmt.close();
            conn.close();

            return "删除成功";

        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
            return "JDBC错误代码" + se.getErrorCode();

        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
            return "Class.forName 错误代码" + e.getMessage();

        }
    }


}
