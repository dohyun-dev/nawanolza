package com.example.nawanolza

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nawanolza.databinding.CharacterRvItemBinding
import com.example.nawanolza.retrofit.CollectionItem

class CharacterRvAdapter(
    private val characterInfo: List<CollectionItem>,
    private val application: Application,
)
:RecyclerView.Adapter<CharacterRvAdapter.MyViewHolder>(){

    inner class MyViewHolder(binding: CharacterRvItemBinding):
        RecyclerView.ViewHolder(binding.root) {
            val characterImg = binding.characterImg
            val level = binding.level
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding: CharacterRvItemBinding = CharacterRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val characterInfo = characterInfo[position]


        if (characterInfo.currentLevel.equals(0)){
            holder.characterImg.setImageDrawable(application.getDrawable(MarkerImageUtil.getDarkImage(characterInfo.characterId)))
        }else{
            holder.characterImg.setImageDrawable(application.getDrawable(MarkerImageUtil.getImage(characterInfo.characterId)))
        }
        holder.level.text = "Lv." + characterInfo.currentLevel
    }

    override fun getItemCount(): Int {
        return characterInfo.size
    }
}