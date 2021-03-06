package edu.aku.hassannaqvi.cs_ict.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.aku.hassannaqvi.cs_ict.contracts.ChildContract
import edu.aku.hassannaqvi.cs_ict.contracts.FamilyMembersContract
import edu.aku.hassannaqvi.cs_ict.contracts.FormsContract
import edu.aku.hassannaqvi.cs_ict.repository.getAllHHChildFromDB
import kotlinx.coroutines.launch

class MainVModel : ViewModel() {

    var familyMemLst = MutableLiveData<MutableList<FamilyMembersContract>>()
        private set

    var childLstU5 = MutableLiveData<MutableList<FamilyMembersContract>>()
        private set

    var childLstU12 = MutableLiveData<MutableList<FamilyMembersContract>>()
        private set

    var checkedItems = MutableLiveData<MutableList<Int>>()
        private set

    var childU5 = MutableLiveData<MutableList<ChildContract>>()
        private set

    fun setFamilyMembers(item: FamilyMembersContract) {
        var lst = familyMemLst.value
        if (lst.isNullOrEmpty())
            lst = mutableListOf()
        lst.add(item)
        familyMemLst.value = lst
    }

    fun updateFamilyMembers(item: FamilyMembersContract) {
        val lst = familyMemLst.value
        lst?.map { if (it.serialno.toInt() == item.serialno.toInt()) item else it }
        familyMemLst.value = lst
    }

    fun setChildU5(item: FamilyMembersContract) {
        var lst = childLstU5.value
        if (lst.isNullOrEmpty())
            lst = mutableListOf()
        lst.add(item)
        childLstU5.value = lst
    }

    fun setChildU12(item: FamilyMembersContract) {
        var lst = childLstU12.value
        if (lst.isNullOrEmpty())
            lst = mutableListOf()
        lst.add(item)
        childLstU12.value = lst
    }

    fun setCheckedItemValues(index: Int) {
        var lst = checkedItems.value
        if (lst.isNullOrEmpty()) {
            lst = mutableListOf()
            lst.add(index)
        } else lst.add(index)

        checkedItems.value = lst
    }

    fun getMemberInfo(index: Int): FamilyMembersContract? {
        return familyMemLst.value?.find { it.serialno.toInt() == index }
    }

    fun getAllUnder5(): Pair<List<Int>?, List<String>?> {
        val family = childLstU5.value
        return Pair(family?.map { it.serialno.toInt() }, family?.map { it.name })
    }

    fun getAllUnder12(): Pair<List<Int>?, List<String>?> {
        val family = childLstU12.value
        return Pair(family?.map { it.serialno.toInt() }, family?.map { it.name })
    }

    fun getAllRespondent(): Pair<List<Int>?, List<String>?> {
        val family = familyMemLst.value?.filter { it -> (it.age.toInt() >= 15) }
        return Pair(family?.map { it.serialno.toInt() }, family?.map { it.name })
    }

    fun getCheckedItemValues(fmItem: Int): Boolean {
        val flag = checkedItems.value?.find { it == fmItem }
        flag?.let { return true } ?: return false
    }

    // Functions for child viewmodel

    fun setChildListU5(item: ChildContract) {
        var lst = childU5.value
        if (lst.isNullOrEmpty())
            lst = mutableListOf()
        lst.add(item)
        childU5.value = lst
    }

    fun populateChildListU5(context: Context, form: FormsContract) {
        viewModelScope.launch {
            var lst = getAllHHChildFromDB(context, form)
            if (lst.isNullOrEmpty()) lst = mutableListOf()
            childU5.value = lst
        }
    }

}