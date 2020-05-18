package com.youtushuju.lingdongapp.database;

import com.youtushuju.lingdongapp.common.Common;
import com.youtushuju.lingdongapp.common.Pair;
import com.youtushuju.lingdongapp.common.STL;

import java.util.ArrayList;
import java.util.List;

public final class QueryBuilder {
    private String m_table;
    private String m_alias;
    private List<String> m_columns;
    private List<Condition> m_conditions;
    private List<Order> m_orders;
    private int m_offset = 0;
    private int m_limit = -1;

    private String m_sql;
    private String m_sqlValues[];

    public QueryBuilder()
    {
    }

    public QueryBuilder Reset()
    {
        m_table = null;
        m_alias = null;
        if(m_columns != null)
            m_columns.clear();
        if(m_conditions != null)
            m_conditions.clear();
        if(m_orders != null)
            m_orders.clear();
        m_offset = -1;
        m_limit = 0;

        m_sql = null;
        m_sqlValues = null;
        return this;
    }

    public QueryBuilder Table(String table)
    {
        m_table = table;
        m_alias = "";
        return this;
    }

    public QueryBuilder Table(String table, String alias)
    {
        m_table = table;
        m_alias = alias;
        return this;
    }

    public QueryBuilder AddColumn(String col)
    {
        if(m_columns == null)
            m_columns = new ArrayList<String>();
        m_columns.add(col);
        return this;
    }

    public QueryBuilder AddColumn(String cols[])
    {
        for (String col : cols)
            AddColumn(col);
        return this;
    }

    public QueryBuilder AddColumn(List<String> cols)
    {
        if(m_columns == null)
            m_columns = new ArrayList<String>();
        m_columns.addAll(cols);
        return this;
    }

    public QueryBuilder SelectColumn(List<String> cols)
    {
        if(m_columns != null)
            m_columns.clear();
        return AddColumn(cols);
    }

    public QueryBuilder SelectColumn(String cols[])
    {
        if(m_columns != null)
            m_columns.clear();
        return AddColumn(cols);
    }

    public QueryBuilder SelectColumn(String col)
    {
        if(m_columns != null)
            m_columns.clear();
        return AddColumn(col);
    }

    public QueryBuilder AddOrder(Order o)
    {
        if(m_orders == null)
            m_orders = new ArrayList<Order>();
        m_orders.add(o);
        return this;
    }

    public QueryBuilder AddOrder(String col, String seq)
    {
        return AddOrder(new Order(col, seq));
    }

    public QueryBuilder AddOrder(String col)
    {
        return AddOrder(new Order(col));
    }

    public QueryBuilder Order(Order o)
    {
        if(m_orders != null)
            m_orders.clear();
        return AddOrder(o);
    }

    public QueryBuilder Order(String col, String seq)
    {
        return Order(new Order(col, seq));
    }

    public QueryBuilder Order(String col)
    {
        return Order(new Order(col));
    }

    public QueryBuilder Limit(int l)
    {
        m_limit = l;
        return this;
    }

    public QueryBuilder Offset(int l)
    {
        m_offset = l;
        return this;
    }

    public QueryBuilder Limit(int o, int l)
    {
        m_offset = o;
        m_limit = l;
        return this;
    }

    public QueryBuilder And(String col, Object value)
    {
        return Where(new Condition(col, value.toString()));
    }

    public QueryBuilder And(String col, String op, Object value)
    {
        return Where(new Condition(col, op, value.toString()));
    }

    public QueryBuilder Or(String col, Object value)
    {
        return Where(new Condition(Condition.ID_QUERY_BUILDER_CONDITION_OR, col, value.toString()));
    }

    public QueryBuilder Or(String col, String op, Object value)
    {
        return Where(new Condition(Condition.ID_QUERY_BUILDER_CONDITION_OR, col, op, value.toString()));
    }

    public QueryBuilder Where(Condition cond)
    {
        if(m_conditions == null)
            m_conditions = new ArrayList<Condition>();
        m_conditions.add(cond);
        return this;
    }

    public QueryBuilder Where(String cond, String col, String op, Object value)
    {
        return Where(new Condition(cond, col, op, value.toString()));
    }

    protected void Generate(String inCol)
    {
        String cols = Common.StringIsEmpty(inCol) ? null : inCol;
        List<String> conds = null;
        List<String> condVals = null;
        List<String> orders = null;

        if(cols != null)
        {
            if(!STL.CollectionIsEmpty(m_columns))
            {
                cols = STL.CollectionJoin(m_columns, ", ");
            }
        }

        if(cols == null)
            cols = "*";

        if(!STL.CollectionIsEmpty(m_conditions))
        {
            for (Condition c : m_conditions)
            {
                c.cond = ""; // 第一个条件去掉逻辑运算符
                Pair<String, String[]> p = c.Generate();
                if(p == null)
                    continue;
                if(conds == null)
                    conds = new ArrayList<String>();
                conds.add(p.first);
                if(condVals == null)
                    condVals = new ArrayList<String>();
                for (String str : p.second)
                    condVals.add(str);
            }
        }

        if(!STL.CollectionIsEmpty(m_orders))
        {
            for (Order c : m_orders)
            {
                Pair<String, String[]> p = c.Generate();
                if(p == null)
                    continue;
                if(orders == null)
                    orders = new ArrayList<String>();
                orders.add(p.first);
            }
        }

        StringBuffer sb = new StringBuffer();
        // base
        sb.append("SELECT ");
        sb.append(cols);
        sb.append(" FROM ").append(m_table);
        if(!Common.StringIsEmpty(m_alias))
            sb.append(" " + m_alias);

        // condition
        if(!STL.CollectionIsEmpty(conds))
        {
            sb.append(" WHERE").append(STL.CollectionJoin(conds, " "));
        }

        // order
        if(!STL.CollectionIsEmpty(m_orders))
        {
            sb.append(" WHERE").append(STL.CollectionJoin(conds, " "));
        }

        // order
        if(!STL.CollectionIsEmpty(orders))
        {
            sb.append(STL.CollectionJoin(orders, ", "));
        }

        // limit
        if(m_limit > 0)
        {
            if(m_offset >= 0)
                sb.append(" LIMIT " + m_offset + " " + m_limit);
            else
                sb.append(" LIMIT " + m_limit);
        }
        else if(m_offset >= 0)
        {
            sb.append("OFFSET " + m_offset);
        }

        m_sql = sb.toString();
        m_sqlValues = STL.CollectionToStringArray(condVals);
    }

    public Pair<String, String[]> Count()
    {
        Generate("COUNT(1)");
        return Pair.make_pair(m_sql, m_sqlValues);
    }

    public Pair<String, String[]> Get()
    {
        Generate(null);
        return Pair.make_pair(m_sql, m_sqlValues);
    }

    public String toString()
    {
        return m_sql;
    }

    // internal
    public interface Part {
        public Pair<String, String[]> Generate();
    }

    public static class Condition implements Part {
        public static final String ID_QUERY_BUILDER_CONDITION_AND = "AND";
        public static final String ID_QUERY_BUILDER_CONDITION_OR = "OR";

        public static final String ID_QUERY_BUILDER_OPERATOR_EQUALS = "=";
        public static final String ID_QUERY_BUILDER_OPERATOR_NOTEQUALS = "<>";
        public static final String ID_QUERY_BUILDER_OPERATOR_LEQUALS = "<=";
        public static final String ID_QUERY_BUILDER_OPERATOR_GEQUALS = ">=";
        public static final String ID_QUERY_BUILDER_OPERATOR_LESS = "<";
        public static final String ID_QUERY_BUILDER_OPERATOR_GRATER = ">";
        public static final String ID_QUERY_BUILDER_OPERATOR_LIKE = "LIKE";
        public static final String ID_QUERY_BUILDER_OPERATOR_BETWEEN_AND = "BETWEEN"; // TODO: not support

        public String cond;
        public String column;
        public String operator;
        public String value;

        public Condition()
        {

        }

        public Condition(String col, String val)
        {
            this(ID_QUERY_BUILDER_CONDITION_AND, col, "=", val);
        }

        public Condition(String col, String op, String val)
        {
            this(ID_QUERY_BUILDER_CONDITION_AND, col, op, val);
        }

        public Condition(String c, String col, String op, String val)
        {
            cond = c;
            operator = op;
            column = col;
            value = val;
        }

        public Pair<String, String[]> Generate()
        {
            String sql = cond + " " + column + " " + operator;
            String vals[] = null;
            if(value != null)
            {
                sql += " ?";
                vals = new String[]{value};
            }
            return Pair.make_pair(sql, vals);
        }
    }

    public static class Order implements Part {
        public static final String ID_QUERY_BUILDER_ORDER_ASC = "ASC";
        public static final String ID_QUERY_BUILDER_ORDER_DESC = "DESC";

        public String column;
        public String sequence = ID_QUERY_BUILDER_ORDER_ASC;

        public Order()
        {

        }

        public Order(String col)
        {
            this(col, ID_QUERY_BUILDER_ORDER_ASC);
        }

        public Order(String col, String seq)
        {
            column = col;
            sequence = seq;
        }

        public Pair<String, String[]> Generate()
        {
            String sql = column;
            if(!Common.StringIsEmpty(sequence))
            {
                sql += " " + sequence;
            }
            return Pair.make_pair(sql, null);
        }
    }
}
