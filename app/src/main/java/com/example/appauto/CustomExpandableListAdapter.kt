package com.example.appauto

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

// 自定义适配器类，继承自 BaseExpandableListAdapter
class CustomExpandableListAdapter(
    private val context: Context,
    private val groupData: List<String>,
    private val childData: List<List<String>>,
    private val specifiedRow: Int // 新增参数，指定要展示的行号
) : BaseExpandableListAdapter() {

    // 获取组的数量
    override fun getGroupCount(): Int {
        return groupData.size
    }

    // 获取指定组的子项数量
    override fun getChildrenCount(groupPosition: Int): Int {
        return childData[groupPosition].size
    }

    // 获取指定组的数据
    override fun getGroup(groupPosition: Int): Any {
        return groupData[groupPosition]
    }

    // 获取指定组中指定子项的数据
    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return childData[groupPosition][childPosition]
    }

    // 获取指定组的 ID
    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    // 获取指定组中指定子项的 ID
    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    // 指示 ID 是否稳定
    override fun hasStableIds(): Boolean {
        return false
    }

    // 获取组视图
    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var view = convertView
        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null)
        }
        val textView = view!!.findViewById<TextView>(android.R.id.text1)
        // 将展示内容换为指定行的数据
        val childList = childData[groupPosition]
        if (specifiedRow in 0 until childList.size) {
            textView.text = childList[specifiedRow]
        } else {
            textView.text = groupData[groupPosition] // 如果指定行号超出范围，展示组数据
        }
        // 设置背景颜色为浅灰色
        view.setBackgroundColor(context.resources.getColor(R.color.light_gray, null))
        // 设置文字颜色为黑色
        textView.setTextColor(context.resources.getColor(android.R.color.black, null))
        // 增大高度
        var layoutParams = view.layoutParams
        if (layoutParams == null) {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        layoutParams.height = 150 // 可根据需要调整高度值
        view.layoutParams = layoutParams
        return view
    }

    // 获取子项视图
    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var view = convertView
        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(android.R.layout.simple_list_item_1, null)
        }
        val textView = view!!.findViewById<TextView>(android.R.id.text1)
        textView.text = childData[groupPosition][childPosition]
        return view
    }

    // 指示子项是否可选择
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}
